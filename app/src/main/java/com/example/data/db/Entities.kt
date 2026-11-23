package com.example.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 1. Users (3NF Root User Entity)
 * PK: user_id, Unique: email
 */
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    val userId: Long = 0,
    val name: String,
    val email: String,
    @ColumnInfo(name = "password_hash")
    val passwordHash: String,
    val phone: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 2. FarmInformation (1:1 with Users)
 * PK: farm_id, FK: user_id -> users.user_id (UNIQUE)
 */
@Entity(
    tableName = "farm_information",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["user_id"], unique = true)]
)
data class FarmInformationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "farm_id")
    val farmId: Long = 0,
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "farm_name")
    val farmName: String,
    val location: String,
    @ColumnInfo(name = "total_land_ha")
    val totalLandHa: Double,
    @ColumnInfo(name = "soil_type")
    val soilType: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 3. Activities (1:N with FarmInformation)
 * PK: activity_id, FK: farm_id -> farm_information.farm_id
 */
@Entity(
    tableName = "activities",
    foreignKeys = [
        ForeignKey(
            entity = FarmInformationEntity::class,
            parentColumns = ["farm_id"],
            childColumns = ["farm_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["farm_id"])]
)
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "activity_id")
    val activityId: Long = 0,
    @ColumnInfo(name = "farm_id")
    val farmId: Long,
    val title: String,
    @ColumnInfo(name = "scheduled_at")
    val scheduledAt: Long,
    val status: String = STATUS_PENDING, // 'pending' | 'completed'
    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_COMPLETED = "completed"
    }
}

/**
 * 4. Expenses (1:N with FarmInformation)
 * PK: expense_id, FK: farm_id -> farm_information.farm_id
 */
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = FarmInformationEntity::class,
            parentColumns = ["farm_id"],
            childColumns = ["farm_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["farm_id"])]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "expense_id")
    val expenseId: Long = 0,
    @ColumnInfo(name = "farm_id")
    val farmId: Long,
    val category: String, // 'Seeds' | 'Fertilizer' | 'Labour' | 'Irrigation' | 'Other'
    val amount: Double,
    @ColumnInfo(name = "expense_date")
    val expenseDate: Long,
    val note: String
) {
    companion object {
        val CATEGORIES = listOf("Seeds", "Fertilizer", "Labour", "Irrigation", "Other")
    }
}

/**
 * 5. PestReports (1:N with FarmInformation)
 * PK: report_id, FK: farm_id -> farm_information.farm_id
 */
@Entity(
    tableName = "pest_reports",
    foreignKeys = [
        ForeignKey(
            entity = FarmInformationEntity::class,
            parentColumns = ["farm_id"],
            childColumns = ["farm_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["farm_id"])]
)
data class PestReportEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "report_id")
    val reportId: Long = 0,
    @ColumnInfo(name = "farm_id")
    val farmId: Long,
    @ColumnInfo(name = "image_ref")
    val imageRef: String,
    @ColumnInfo(name = "detected_issue")
    val detectedIssue: String,
    val recommendation: String,
    @ColumnInfo(name = "reported_at")
    val reportedAt: Long = System.currentTimeMillis()
)

/**
 * 6. WeatherHistory (1:N with FarmInformation)
 * PK: weather_id, FK: farm_id -> farm_information.farm_id
 */
@Entity(
    tableName = "weather_history",
    foreignKeys = [
        ForeignKey(
            entity = FarmInformationEntity::class,
            parentColumns = ["farm_id"],
            childColumns = ["farm_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["farm_id"])]
)
data class WeatherHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "weather_id")
    val weatherId: Long = 0,
    @ColumnInfo(name = "farm_id")
    val farmId: Long,
    @ColumnInfo(name = "record_date")
    val recordDate: Long = System.currentTimeMillis(),
    val temperature: Double,
    val humidity: Int,
    @ColumnInfo(name = "rain_chance")
    val rainChance: Int,
    val condition: String
)

/**
 * 7. AIRecommendations (1:N with FarmInformation)
 * PK: rec_id, FK: farm_id -> farm_information.farm_id
 */
@Entity(
    tableName = "ai_recommendations",
    foreignKeys = [
        ForeignKey(
            entity = FarmInformationEntity::class,
            parentColumns = ["farm_id"],
            childColumns = ["farm_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["farm_id"])]
)
data class AIRecommendationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rec_id")
    val recId: Long = 0,
    @ColumnInfo(name = "farm_id")
    val farmId: Long,
    val source: String, // 'weather_engine' | 'ai_chat'
    @ColumnInfo(name = "query_text")
    val queryText: String,
    @ColumnInfo(name = "response_text")
    val responseText: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val SOURCE_WEATHER_ENGINE = "weather_engine"
        const val SOURCE_AI_CHAT = "ai_chat"
    }
}
