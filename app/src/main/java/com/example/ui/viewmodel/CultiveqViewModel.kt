package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.CultiveqAiService
import com.example.data.ai.StructuredPestDiagnosis
import com.example.data.auth.SessionManager
import com.example.data.db.AIRecommendationEntity
import com.example.data.db.ActivityEntity
import com.example.data.db.CultiveqDatabase
import com.example.data.db.ExpenseEntity
import com.example.data.db.FarmInformationEntity
import com.example.data.db.PestReportEntity
import com.example.data.db.SqliteSchemaTable
import com.example.data.db.UserEntity
import com.example.data.db.WeatherHistoryEntity
import com.example.data.network.WeatherService
import com.example.data.repository.CultiveqRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "CultiveqViewModel"

sealed interface AuthState {
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data class Authenticated(
        val user: UserEntity,
        val farm: FarmInformationEntity?
    ) : AuthState
}

sealed interface AiOpState {
    data object Idle : AiOpState
    data object Loading : AiOpState
    data class Streaming(val partialText: String) : AiOpState
    data class Success(val message: String) : AiOpState
    data class Error(val message: String) : AiOpState
}

sealed interface PestOpState {
    data object Idle : PestOpState
    data object Loading : PestOpState
    data class Success(val diagnosis: StructuredPestDiagnosis) : PestOpState
    data class Error(val message: String) : PestOpState
}

enum class CultiveqTab(val label: String) {
    FARM("Farm"),
    TASKS("Tasks"),
    ADVISOR("Advisor"),
    PEST("Pest Scan"),
    FINANCE("Finance")
}

@OptIn(ExperimentalCoroutinesApi::class)
class CultiveqViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CultiveqRepository by lazy {
        val db = CultiveqDatabase.getInstance(application)
        CultiveqRepository(
            dao = db.cultiveqDao(),
            sessionManager = SessionManager(application),
            weatherService = WeatherService(),
            aiService = CultiveqAiService()
        )
    }

    val authState: StateFlow<AuthState> = combine(
        repository.currentUserFlow,
        repository.currentFarmFlow
    ) { user, farm ->
        if (user == null) AuthState.Unauthenticated else AuthState.Authenticated(user, farm)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AuthState.Loading
    )

    private val currentFarmIdFlow = repository.currentFarmFlow.flatMapLatest { farm ->
        flowOf(farm?.farmId)
    }

    val activities: StateFlow<List<ActivityEntity>> = currentFarmIdFlow.flatMapLatest { farmId ->
        if (farmId == null) flowOf(emptyList()) else repository.observeActivities(farmId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = currentFarmIdFlow.flatMapLatest { farmId ->
        if (farmId == null) flowOf(emptyList()) else repository.observeExpenses(farmId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pestReports: StateFlow<List<PestReportEntity>> = currentFarmIdFlow.flatMapLatest { farmId ->
        if (farmId == null) flowOf(emptyList()) else repository.observePestReports(farmId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val weatherHistory: StateFlow<List<WeatherHistoryEntity>> = currentFarmIdFlow.flatMapLatest { farmId ->
        if (farmId == null) flowOf(emptyList()) else repository.observeWeatherHistory(farmId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recommendations: StateFlow<List<AIRecommendationEntity>> = currentFarmIdFlow.flatMapLatest { farmId ->
        if (farmId == null) flowOf(emptyList()) else repository.observeRecommendations(farmId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedTab = MutableStateFlow(CultiveqTab.FARM)
    val selectedTab: StateFlow<CultiveqTab> = _selectedTab.asStateFlow()

    private val _advisorSubTab = MutableStateFlow(0) // 0 = Weather Engine, 1 = AI Chat
    val advisorSubTab: StateFlow<Int> = _advisorSubTab.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authBusy = MutableStateFlow(false)
    val authBusy: StateFlow<Boolean> = _authBusy.asStateFlow()

    private val _weatherEngineState = MutableStateFlow<AiOpState>(AiOpState.Idle)
    val weatherEngineState: StateFlow<AiOpState> = _weatherEngineState.asStateFlow()

    private val _weatherSourceLabel = MutableStateFlow<String?>(null)
    val weatherSourceLabel: StateFlow<String?> = _weatherSourceLabel.asStateFlow()

    private val _chatState = MutableStateFlow<AiOpState>(AiOpState.Idle)
    val chatState: StateFlow<AiOpState> = _chatState.asStateFlow()

    private val _pendingChatQuestion = MutableStateFlow<String?>(null)
    val pendingChatQuestion: StateFlow<String?> = _pendingChatQuestion.asStateFlow()

    private val _pestState = MutableStateFlow<PestOpState>(PestOpState.Idle)
    val pestState: StateFlow<PestOpState> = _pestState.asStateFlow()

    private val _schemaDdl = MutableStateFlow<List<SqliteSchemaTable>>(emptyList())
    val schemaDdl: StateFlow<List<SqliteSchemaTable>> = _schemaDdl.asStateFlow()

    private val _showSchemaInspector = MutableStateFlow(false)
    val showSchemaInspector: StateFlow<Boolean> = _showSchemaInspector.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    fun selectTab(tab: CultiveqTab) {
        _selectedTab.value = tab
    }

    fun openWeatherEngine() {
        _advisorSubTab.value = 0
        _selectedTab.value = CultiveqTab.ADVISOR
    }

    fun openAiChat() {
        _advisorSubTab.value = 1
        _selectedTab.value = CultiveqTab.ADVISOR
    }

    fun selectAdvisorSubTab(index: Int) {
        _advisorSubTab.value = index
    }

    fun openSchemaInspector() {
        _showSchemaInspector.value = true
        refreshSchemaDdl()
    }

    fun closeSchemaInspector() {
        _showSchemaInspector.value = false
    }

    fun refreshSchemaDdl() {
        viewModelScope.launch {
            try {
                _schemaDdl.value = repository.getLiveSchemaDdl()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read sqlite_master schema", e)
            }
        }
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    fun signUp(
        name: String,
        email: String,
        password: String,
        phone: String,
        farmName: String,
        location: String,
        totalLandHaText: String,
        soilType: String
    ) {
        viewModelScope.launch {
            _authBusy.value = true
            _authError.value = null
            val landHa = totalLandHaText.toDoubleOrNull() ?: 2.0
            val result = repository.signUp(
                name = name,
                email = email,
                password = password,
                phone = phone,
                farmName = farmName,
                location = location,
                totalLandHa = landHa,
                soilType = soilType
            )
            _authBusy.value = false
            result.onFailure { _authError.value = it.message ?: "Sign up failed." }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authBusy.value = true
            _authError.value = null
            val result = repository.login(email, password)
            _authBusy.value = false
            result.onFailure { _authError.value = it.message ?: "Login failed." }
        }
    }

    fun loadDemoFarmer() {
        viewModelScope.launch {
            _authBusy.value = true
            _authError.value = null
            val result = repository.loadDemoFarmer()
            _authBusy.value = false
            result.onFailure { _authError.value = it.message ?: "Failed to load demo farmer." }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _weatherEngineState.value = AiOpState.Idle
            _chatState.value = AiOpState.Idle
            _pestState.value = PestOpState.Idle
            _selectedTab.value = CultiveqTab.FARM
        }
    }

    fun saveFarmProfile(
        farmName: String,
        location: String,
        totalLandHaText: String,
        soilType: String
    ) {
        val current = authState.value as? AuthState.Authenticated ?: return
        val landHa = totalLandHaText.toDoubleOrNull()
        if (landHa == null || landHa <= 0.0) {
            _statusBanner.value = "Enter a valid positive land area in hectares."
            return
        }
        viewModelScope.launch {
            val result = repository.saveFarmProfile(
                userId = current.user.userId,
                existingFarmId = current.farm?.farmId,
                farmName = farmName,
                location = location,
                totalLandHa = landHa,
                soilType = soilType
            )
            result.onSuccess {
                _statusBanner.value = "Farm profile saved to SQLite (farm_information)."
            }.onFailure {
                _statusBanner.value = it.message ?: "Failed to save farm profile."
            }
        }
    }

    fun addActivity(title: String, scheduledAt: Long) {
        val farm = (authState.value as? AuthState.Authenticated)?.farm ?: return
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addActivity(farm.farmId, title, scheduledAt)
            _statusBanner.value = "Activity scheduled."
        }
    }

    fun toggleActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            repository.toggleActivityStatus(activity)
        }
    }

    fun deleteActivity(activityId: Long) {
        viewModelScope.launch {
            repository.deleteActivity(activityId)
        }
    }

    fun addExpense(category: String, amountText: String, expenseDate: Long, note: String) {
        val farm = (authState.value as? AuthState.Authenticated)?.farm ?: return
        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            _statusBanner.value = "Enter a valid positive expense amount."
            return
        }
        viewModelScope.launch {
            repository.addExpense(farm.farmId, category, amount, expenseDate, note)
            _statusBanner.value = "Expense logged under $category."
        }
    }

    fun deleteExpense(expenseId: Long) {
        viewModelScope.launch {
            repository.deleteExpense(expenseId)
        }
    }

    fun runWeatherRecommendationEngine() {
        val farm = (authState.value as? AuthState.Authenticated)?.farm ?: run {
            _weatherEngineState.value = AiOpState.Error("Create your farm profile first.")
            return
        }
        viewModelScope.launch {
            _weatherEngineState.value = AiOpState.Loading
            try {
                val (snapshot, savedWeather) = repository.fetchWeatherAndSave(farm)
                _weatherSourceLabel.value = if (snapshot.isLiveTelemetry) {
                    "Live Open-Meteo • ${snapshot.resolvedPlace}"
                } else {
                    "Regional Forecast Model • ${snapshot.resolvedPlace}"
                }
                val adviceResult = repository.generateAndSaveWeatherAdvice(farm, savedWeather)
                adviceResult.onSuccess { rec ->
                    _weatherEngineState.value = AiOpState.Success(rec.responseText)
                }.onFailure { err ->
                    _weatherEngineState.value = AiOpState.Error(
                        err.localizedMessage ?: "Failed to generate weather recommendation."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Weather engine error", e)
                _weatherEngineState.value = AiOpState.Error(
                    e.localizedMessage ?: "Weather engine error."
                )
            }
        }
    }

    fun sendChatMessage(question: String) {
        val farm = (authState.value as? AuthState.Authenticated)?.farm ?: run {
            _chatState.value = AiOpState.Error("Create your farm profile first.")
            return
        }
        val trimmed = question.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            _pendingChatQuestion.value = trimmed
            _chatState.value = AiOpState.Loading
            val latestWeather = weatherHistory.value.firstOrNull()
            var finalAnswer = ""
            try {
                repository.streamChatAnswer(trimmed, farm, latestWeather).collect { partial ->
                    finalAnswer = partial
                    _chatState.value = AiOpState.Streaming(partial)
                }
                if (finalAnswer.isNotBlank()) {
                    repository.saveChatRecommendation(farm.farmId, trimmed, finalAnswer)
                    _chatState.value = AiOpState.Success(finalAnswer)
                    _pendingChatQuestion.value = null
                } else {
                    _chatState.value = AiOpState.Error("Model returned an empty response.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Chat timed out", e)
                _chatState.value = AiOpState.Error("AI assistant timed out after 60s. Please retry.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI chat failed", e)
                _chatState.value = AiOpState.Error(e.localizedMessage ?: "AI chat failed.")
            }
        }
    }

    fun analyzePestReport(
        selectedImageUri: Uri?,
        selectedSymptoms: List<String>,
        notes: String
    ) {
        val farm = (authState.value as? AuthState.Authenticated)?.farm ?: run {
            _pestState.value = PestOpState.Error("Create your farm profile first.")
            return
        }
        if (selectedImageUri == null && selectedSymptoms.isEmpty() && notes.isBlank()) {
            _pestState.value = PestOpState.Error("Select a crop image or at least one symptom.")
            return
        }

        viewModelScope.launch {
            _pestState.value = PestOpState.Loading
            try {
                val bitmap = selectedImageUri?.let { decodeScaledBitmap(it) }
                val imageRef = selectedImageUri?.toString()
                    ?: "symptoms://${selectedSymptoms.joinToString("_").lowercase().replace(" ", "_")}"

                val result = repository.diagnoseAndSavePestReport(
                    farm = farm,
                    imageRef = imageRef,
                    symptoms = selectedSymptoms,
                    notes = notes,
                    bitmap = bitmap
                )
                result.onSuccess { (diagnosis, _) ->
                    _pestState.value = PestOpState.Success(diagnosis)
                }.onFailure { err ->
                    _pestState.value = PestOpState.Error(
                        err.localizedMessage ?: "Pest diagnosis failed."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Pest diagnosis failed", e)
                _pestState.value = PestOpState.Error(e.localizedMessage ?: "Pest diagnosis failed.")
            }
        }
    }

    private suspend fun decodeScaledBitmap(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val resolver = getApplication<Application>().contentResolver
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, boundsOptions) }

            val maxDim = 1024
            var sampleSize = 1
            val w = boundsOptions.outWidth
            val h = boundsOptions.outHeight
            while (w / sampleSize > maxDim || h / sampleSize > maxDim) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, decodeOptions) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode selected pest image", e)
            null
        }
    }
}
