package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class SqliteSchemaTable(
    val name: String,
    val sql: String
)

@Dao
interface CultiveqDao {

    // --- 1. Users ---
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE user_id = :userId LIMIT 1")
    fun observeUserById(userId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE user_id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("SELECT * FROM users ORDER BY user_id ASC")
    fun observeAllUsers(): Flow<List<UserEntity>>

    // --- 2. FarmInformation (1:1 with Users) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarm(farm: FarmInformationEntity): Long

    @Update
    suspend fun updateFarm(farm: FarmInformationEntity)

    @Query("SELECT * FROM farm_information WHERE user_id = :userId LIMIT 1")
    fun observeFarmByUserId(userId: Long): Flow<FarmInformationEntity?>

    @Query("SELECT * FROM farm_information WHERE user_id = :userId LIMIT 1")
    suspend fun getFarmByUserId(userId: Long): FarmInformationEntity?

    // --- 3. Activities (1:N with FarmInformation) ---
    @Insert
    suspend fun insertActivity(activity: ActivityEntity): Long

    @Update
    suspend fun updateActivity(activity: ActivityEntity)

    @Query("DELETE FROM activities WHERE activity_id = :activityId")
    suspend fun deleteActivityById(activityId: Long)

    @Query("SELECT * FROM activities WHERE farm_id = :farmId ORDER BY scheduled_at ASC")
    fun observeActivitiesByFarm(farmId: Long): Flow<List<ActivityEntity>>

    // --- 4. Expenses (1:N with FarmInformation) ---
    @Insert
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Query("DELETE FROM expenses WHERE expense_id = :expenseId")
    suspend fun deleteExpenseById(expenseId: Long)

    @Query("SELECT * FROM expenses WHERE farm_id = :farmId ORDER BY expense_date DESC")
    fun observeExpensesByFarm(farmId: Long): Flow<List<ExpenseEntity>>

    // --- 5. PestReports (1:N with FarmInformation) ---
    @Insert
    suspend fun insertPestReport(report: PestReportEntity): Long

    @Query("SELECT * FROM pest_reports WHERE farm_id = :farmId ORDER BY reported_at DESC")
    fun observePestReportsByFarm(farmId: Long): Flow<List<PestReportEntity>>

    // --- 6. WeatherHistory (1:N with FarmInformation) ---
    @Insert
    suspend fun insertWeatherRecord(weather: WeatherHistoryEntity): Long

    @Query("SELECT * FROM weather_history WHERE farm_id = :farmId ORDER BY record_date DESC")
    fun observeWeatherHistoryByFarm(farmId: Long): Flow<List<WeatherHistoryEntity>>

    // --- 7. AIRecommendations (1:N with FarmInformation) ---
    @Insert
    suspend fun insertRecommendation(rec: AIRecommendationEntity): Long

    @Query("SELECT * FROM ai_recommendations WHERE farm_id = :farmId ORDER BY created_at DESC")
    fun observeRecommendationsByFarm(farmId: Long): Flow<List<AIRecommendationEntity>>

    @Query("SELECT * FROM ai_recommendations WHERE farm_id = :farmId AND source = :source ORDER BY created_at DESC")
    fun observeRecommendationsBySource(farmId: Long, source: String): Flow<List<AIRecommendationEntity>>

    // --- Live SQLite Schema Inspection ---
    @Query(
        "SELECT name, sql FROM sqlite_master " +
            "WHERE type = 'table' AND name NOT LIKE 'sqlite_%' " +
            "AND name NOT LIKE 'room_%' AND name != 'android_metadata' " +
            "AND sql IS NOT NULL ORDER BY name ASC"
    )
    suspend fun getSqliteTableDefinitions(): List<SqliteSchemaTable>
}
