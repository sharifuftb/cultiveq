package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        FarmInformationEntity::class,
        ActivityEntity::class,
        ExpenseEntity::class,
        PestReportEntity::class,
        WeatherHistoryEntity::class,
        AIRecommendationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CultiveqDatabase : RoomDatabase() {
    abstract fun cultiveqDao(): CultiveqDao

    companion object {
        @Volatile
        private var INSTANCE: CultiveqDatabase? = null

        fun getInstance(context: Context): CultiveqDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CultiveqDatabase::class.java,
                    "cultiveq_v2.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
