package com.example.data.repository

import android.graphics.Bitmap
import com.example.data.ai.CultiveqAiService
import com.example.data.ai.StructuredPestDiagnosis
import com.example.data.auth.PasswordHasher
import com.example.data.auth.SessionManager
import com.example.data.db.AIRecommendationEntity
import com.example.data.db.ActivityEntity
import com.example.data.db.CultiveqDao
import com.example.data.db.ExpenseEntity
import com.example.data.db.FarmInformationEntity
import com.example.data.db.PestReportEntity
import com.example.data.db.SqliteSchemaTable
import com.example.data.db.UserEntity
import com.example.data.db.WeatherHistoryEntity
import com.example.data.network.WeatherService
import com.example.data.network.WeatherSnapshotResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class CultiveqRepository(
    private val dao: CultiveqDao,
    private val sessionManager: SessionManager,
    private val weatherService: WeatherService,
    private val aiService: CultiveqAiService
) {

    val currentUserIdFlow: Flow<Long?> = sessionManager.currentUserIdFlow

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentUserFlow: Flow<UserEntity?> = currentUserIdFlow.flatMapLatest { id ->
        if (id == null) flowOf(null) else dao.observeUserById(id)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentFarmFlow: Flow<FarmInformationEntity?> = currentUserIdFlow.flatMapLatest { id ->
        if (id == null) flowOf(null) else dao.observeFarmByUserId(id)
    }

    fun observeActivities(farmId: Long): Flow<List<ActivityEntity>> = dao.observeActivitiesByFarm(farmId)
    fun observeExpenses(farmId: Long): Flow<List<ExpenseEntity>> = dao.observeExpensesByFarm(farmId)
    fun observePestReports(farmId: Long): Flow<List<PestReportEntity>> = dao.observePestReportsByFarm(farmId)
    fun observeWeatherHistory(farmId: Long): Flow<List<WeatherHistoryEntity>> = dao.observeWeatherHistoryByFarm(farmId)
    fun observeRecommendations(farmId: Long): Flow<List<AIRecommendationEntity>> = dao.observeRecommendationsByFarm(farmId)

    suspend fun signUp(
        name: String,
        email: String,
        password: String,
        phone: String,
        farmName: String,
        location: String,
        totalLandHa: Double,
        soilType: String
    ): Result<UserEntity> {
        val normalizedEmail = email.trim().lowercase()
        if (name.isBlank() || normalizedEmail.isBlank() || password.length < 6) {
            return Result.failure(IllegalArgumentException("Enter a valid name, email, and password (min 6 chars)."))
        }
        val existing = dao.getUserByEmail(normalizedEmail)
        if (existing != null) {
            return Result.failure(IllegalStateException("An account with this email already exists."))
        }
        val user = UserEntity(
            name = name.trim(),
            email = normalizedEmail,
            passwordHash = PasswordHasher.hashPassword(password),
            phone = phone.trim().ifBlank { "+880-1700-000000" }
        )
        val userId = dao.insertUser(user)
        val createdUser = user.copy(userId = userId)

        // Create 1:1 FarmInformation record
        dao.insertFarm(
            FarmInformationEntity(
                userId = userId,
                farmName = farmName.trim().ifBlank { "${name.trim()}'s Farm" },
                location = location.trim().ifBlank { "Bogura, Bangladesh" },
                totalLandHa = totalLandHa.coerceAtLeast(0.1),
                soilType = soilType.ifBlank { "Loamy" }
            )
        )
        sessionManager.setLoggedInUser(userId)
        return Result.success(createdUser)
    }

    suspend fun login(email: String, password: String): Result<UserEntity> {
        val normalizedEmail = email.trim().lowercase()
        val user = dao.getUserByEmail(normalizedEmail)
            ?: return Result.failure(IllegalArgumentException("No account found for $normalizedEmail."))
        if (!PasswordHasher.verifyPassword(password, user.passwordHash)) {
            return Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
        }
        sessionManager.setLoggedInUser(user.userId)
        return Result.success(user)
    }

    suspend fun logout() {
        sessionManager.clearSession()
    }

    suspend fun loadDemoFarmer(): Result<UserEntity> {
        val demoEmail = "amina.demo@cultiveq.farm"
        val existing = dao.getUserByEmail(demoEmail)
        if (existing != null) {
            sessionManager.setLoggedInUser(existing.userId)
            return Result.success(existing)
        }

        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L
        val userId = dao.insertUser(
            UserEntity(
                name = "Amina Rahman",
                email = demoEmail,
                passwordHash = PasswordHasher.hashPassword("CultiveqDemo2026"),
                phone = "+880 1712-458920",
                createdAt = now - 14 * dayMs
            )
        )

        val farmId = dao.insertFarm(
            FarmInformationEntity(
                userId = userId,
                farmName = "Green Valley Terrace Farm",
                location = "Bogura, Bangladesh",
                totalLandHa = 4.5,
                soilType = "Loamy Alluvial",
                createdAt = now - 14 * dayMs
            )
        )

        // Seed Activities
        dao.insertActivity(
            ActivityEntity(
                farmId = farmId,
                title = "Drip irrigation check — North Paddy Plot",
                scheduledAt = now - 2 * dayMs,
                status = ActivityEntity.STATUS_COMPLETED,
                completedAt = now - 2 * dayMs + 7_200_000L,
                createdAt = now - 5 * dayMs
            )
        )
        dao.insertActivity(
            ActivityEntity(
                farmId = farmId,
                title = "Apply NPK basal fertilizer (Plot B)",
                scheduledAt = now - dayMs,
                status = ActivityEntity.STATUS_COMPLETED,
                completedAt = now - dayMs + 10_800_000L,
                createdAt = now - 4 * dayMs
            )
        )
        dao.insertActivity(
            ActivityEntity(
                farmId = farmId,
                title = "Neem oil foliar spray before dusk",
                scheduledAt = now + 6 * 3_600_000L,
                status = ActivityEntity.STATUS_PENDING,
                createdAt = now - dayMs
            )
        )
        dao.insertActivity(
            ActivityEntity(
                farmId = farmId,
                title = "Soil pH & moisture sensor calibration",
                scheduledAt = now + dayMs,
                status = ActivityEntity.STATUS_PENDING,
                createdAt = now
            )
        )

        // Seed Expenses across all 5 categories
        dao.insertExpense(ExpenseEntity(farmId = farmId, category = "Seeds", amount = 240.0, expenseDate = now - 10 * dayMs, note = "High-yield BRRI dhan-29 paddy seeds"))
        dao.insertExpense(ExpenseEntity(farmId = farmId, category = "Fertilizer", amount = 185.5, expenseDate = now - 7 * dayMs, note = "Urea + TSP balanced topsoil mix"))
        dao.insertExpense(ExpenseEntity(farmId = farmId, category = "Labour", amount = 310.0, expenseDate = now - 5 * dayMs, note = "4-day transplanting crew (Plot A & B)"))
        dao.insertExpense(ExpenseEntity(farmId = farmId, category = "Irrigation", amount = 125.0, expenseDate = now - 3 * dayMs, note = "Solar pump maintenance & drip valves"))
        dao.insertExpense(ExpenseEntity(farmId = farmId, category = "Other", amount = 64.0, expenseDate = now - dayMs, note = "Soil test kit & neem extract concentrate"))

        // Seed WeatherHistory
        val weatherId = dao.insertWeatherRecord(
            WeatherHistoryEntity(
                farmId = farmId,
                recordDate = now - 3_600_000L,
                temperature = 27.4,
                humidity = 74,
                rainChance = 35,
                condition = "Partly Cloudy"
            )
        )
        dao.insertWeatherRecord(
            WeatherHistoryEntity(
                farmId = farmId,
                recordDate = now - dayMs,
                temperature = 29.1,
                humidity = 68,
                rainChance = 20,
                condition = "Warm & Clear"
            )
        )

        // Seed PestReport
        dao.insertPestReport(
            PestReportEntity(
                farmId = farmId,
                imageRef = "symptom_scan://rice_leaf_folder",
                detectedIssue = "Early Leaf Folder Stress (Moderate)",
                recommendation = "Severity: Moderate (86% match)\nImmediate Action: Inspect rolled upper leaves in Plot B and drain standing water briefly if excessive.\nOrganic / Targeted Treatment: Apply 3% cold-pressed neem oil + soap emulsifier at dusk.\nPrevention: Maintain balanced nitrogen doses and encourage spider/dragonfly habitat.",
                reportedAt = now - 2 * dayMs
            )
        )

        // Seed AIRecommendations
        if (weatherId > 0) {
            dao.insertRecommendation(
                AIRecommendationEntity(
                    farmId = farmId,
                    source = AIRecommendationEntity.SOURCE_WEATHER_ENGINE,
                    queryText = "Weather Snapshot: 27.4°C, 74% humidity, 35% rain chance (Partly Cloudy) at Bogura, Bangladesh",
                    responseText = "• Irrigation: Loamy Alluvial soil retains moisture well—hold deep flooding today and schedule light morning drip.\n• Field Window: 35% rain chance means morning foliar spraying is safe before midday wind.\n• Watchout: 74% humidity elevates leaf-sheath fungal risk; keep field bunds well-ventilated.",
                    createdAt = now - 3_500_000L
                )
            )
        }
        dao.insertRecommendation(
            AIRecommendationEntity(
                farmId = farmId,
                source = AIRecommendationEntity.SOURCE_AI_CHAT,
                queryText = "How should I split nitrogen fertilizer across my 4.5 ha loamy alluvial plot?",
                responseText = "For 4.5 ha of Loamy Alluvial soil, split nitrogen into 3 stages: 30% basal during soil prep, 40% at active tillering (21 days), and 30% at panicle initiation. Applying smaller splits prevents leaching and reduces lush leaf-folder attraction.",
                createdAt = now - dayMs
            )
        )

        sessionManager.setLoggedInUser(userId)
        val created = dao.getUserById(userId)!!
        return Result.success(created)
    }

    suspend fun saveFarmProfile(
        userId: Long,
        existingFarmId: Long?,
        farmName: String,
        location: String,
        totalLandHa: Double,
        soilType: String
    ): Result<FarmInformationEntity> {
        if (farmName.isBlank() || location.isBlank() || totalLandHa <= 0.0) {
            return Result.failure(IllegalArgumentException("Farm name, location, and positive land area are required."))
        }
        val entity = if (existingFarmId != null) {
            val current = dao.getFarmByUserId(userId)
            FarmInformationEntity(
                farmId = existingFarmId,
                userId = userId,
                farmName = farmName.trim(),
                location = location.trim(),
                totalLandHa = totalLandHa,
                soilType = soilType,
                createdAt = current?.createdAt ?: System.currentTimeMillis()
            ).also { dao.updateFarm(it) }
        } else {
            val newEntity = FarmInformationEntity(
                userId = userId,
                farmName = farmName.trim(),
                location = location.trim(),
                totalLandHa = totalLandHa,
                soilType = soilType
            )
            val id = dao.insertFarm(newEntity)
            newEntity.copy(farmId = id)
        }
        return Result.success(entity)
    }

    suspend fun addActivity(farmId: Long, title: String, scheduledAt: Long) {
        dao.insertActivity(
            ActivityEntity(
                farmId = farmId,
                title = title.trim(),
                scheduledAt = scheduledAt,
                status = ActivityEntity.STATUS_PENDING
            )
        )
    }

    suspend fun toggleActivityStatus(activity: ActivityEntity) {
        val isNowCompleted = activity.status != ActivityEntity.STATUS_COMPLETED
        dao.updateActivity(
            activity.copy(
                status = if (isNowCompleted) ActivityEntity.STATUS_COMPLETED else ActivityEntity.STATUS_PENDING,
                completedAt = if (isNowCompleted) System.currentTimeMillis() else null
            )
        )
    }

    suspend fun deleteActivity(activityId: Long) = dao.deleteActivityById(activityId)

    suspend fun addExpense(
        farmId: Long,
        category: String,
        amount: Double,
        expenseDate: Long,
        note: String
    ) {
        dao.insertExpense(
            ExpenseEntity(
                farmId = farmId,
                category = category,
                amount = amount,
                expenseDate = expenseDate,
                note = note.trim()
            )
        )
    }

    suspend fun deleteExpense(expenseId: Long) = dao.deleteExpenseById(expenseId)

    suspend fun fetchWeatherAndSave(farm: FarmInformationEntity): Pair<WeatherSnapshotResult, WeatherHistoryEntity> {
        val snapshot = weatherService.fetchWeatherForLocation(farm.location)
        val entity = WeatherHistoryEntity(
            farmId = farm.farmId,
            recordDate = System.currentTimeMillis(),
            temperature = snapshot.temperatureC,
            humidity = snapshot.humidityPercent,
            rainChance = snapshot.rainChancePercent,
            condition = snapshot.condition
        )
        val id = dao.insertWeatherRecord(entity)
        return snapshot to entity.copy(weatherId = id)
    }

    suspend fun generateAndSaveWeatherAdvice(
        farm: FarmInformationEntity,
        weather: WeatherHistoryEntity
    ): Result<AIRecommendationEntity> {
        val aiResult = aiService.generateWeatherAdvice(farm, weather)
        return aiResult.map { advice ->
            val rec = AIRecommendationEntity(
                farmId = farm.farmId,
                source = AIRecommendationEntity.SOURCE_WEATHER_ENGINE,
                queryText = "Weather Snapshot: ${weather.temperature}°C, ${weather.humidity}% humidity, ${weather.rainChance}% rain (${weather.condition}) at ${farm.location}",
                responseText = advice
            )
            val id = dao.insertRecommendation(rec)
            rec.copy(recId = id)
        }
    }

    fun streamChatAnswer(
        question: String,
        farm: FarmInformationEntity,
        latestWeather: WeatherHistoryEntity?
    ): Flow<String> = aiService.streamFarmChatResponse(question, farm, latestWeather)

    suspend fun saveChatRecommendation(
        farmId: Long,
        question: String,
        answer: String
    ) {
        dao.insertRecommendation(
            AIRecommendationEntity(
                farmId = farmId,
                source = AIRecommendationEntity.SOURCE_AI_CHAT,
                queryText = question.trim(),
                responseText = answer.trim()
            )
        )
    }

    suspend fun diagnoseAndSavePestReport(
        farm: FarmInformationEntity,
        imageRef: String,
        symptoms: List<String>,
        notes: String,
        bitmap: Bitmap?
    ): Result<Pair<StructuredPestDiagnosis, PestReportEntity>> {
        val result = aiService.analyzePest(farm, symptoms, notes, bitmap)
        return result.map { diagnosis ->
            val entity = PestReportEntity(
                farmId = farm.farmId,
                imageRef = imageRef,
                detectedIssue = "${diagnosis.detectedIssue} (${diagnosis.severity})",
                recommendation = diagnosis.toFormattedRecommendation()
            )
            val id = dao.insertPestReport(entity)
            diagnosis to entity.copy(reportId = id)
        }
    }

    suspend fun getLiveSchemaDdl(): List<SqliteSchemaTable> = dao.getSqliteTableDefinitions()
}
