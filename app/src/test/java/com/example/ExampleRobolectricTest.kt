package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.CultiveqAiService
import com.example.data.auth.PasswordHasher
import com.example.data.auth.SessionManager
import com.example.data.db.AIRecommendationEntity
import com.example.data.db.ActivityEntity
import com.example.data.db.CultiveqDatabase
import com.example.data.network.WeatherService
import com.example.data.repository.CultiveqRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: CultiveqDatabase
    private lateinit var repository: CultiveqRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, CultiveqDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = CultiveqRepository(
            dao = db.cultiveqDao(),
            sessionManager = SessionManager(context),
            weatherService = WeatherService(),
            aiService = CultiveqAiService()
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `app name matches Cultiveq`() {
        assertEquals("Cultiveq", context.getString(R.string.app_name))
    }

    @Test
    fun `password hasher uses salted PBKDF2 and verifies securely`() {
        val hash1 = PasswordHasher.hashPassword("Harvest2026!")
        val hash2 = PasswordHasher.hashPassword("Harvest2026!")
        assertTrue(hash1 != hash2) // Unique random salt per hash
        assertTrue(PasswordHasher.verifyPassword("Harvest2026!", hash1))
        assertFalse(PasswordHasher.verifyPassword("WrongPass", hash1))
    }

    @Test
    fun `sqlite schema creates all 7 normalized 3NF tables with foreign keys`() = runTest {
        val tables = repository.getLiveSchemaDdl()
        val names = tables.map { it.name }.toSet()
        assertEquals(
            setOf(
                "users",
                "farm_information",
                "activities",
                "expenses",
                "pest_reports",
                "weather_history",
                "ai_recommendations"
            ),
            names
        )
        val farmDdl = tables.first { it.name == "farm_information" }.sql
        assertTrue(farmDdl.contains("FOREIGN KEY"))
        assertTrue(farmDdl.contains("users"))
    }

    @Test
    fun `loadDemoFarmer seeds 3NF relational records and isolates per-user farm data`() = runTest {
        val demoUser = repository.loadDemoFarmer().getOrThrow()
        assertEquals("Amina Rahman", demoUser.name)

        val demoFarm = db.cultiveqDao().getFarmByUserId(demoUser.userId)
        assertNotNull(demoFarm)
        assertEquals("Green Valley Terrace Farm", demoFarm!!.farmName)

        val demoActivities = repository.observeActivities(demoFarm.farmId).first()
        val demoExpenses = repository.observeExpenses(demoFarm.farmId).first()
        val demoWeather = repository.observeWeatherHistory(demoFarm.farmId).first()
        val demoPests = repository.observePestReports(demoFarm.farmId).first()
        val demoRecs = repository.observeRecommendations(demoFarm.farmId).first()

        assertTrue(demoActivities.isNotEmpty())
        assertTrue(demoExpenses.size == 5)
        assertTrue(demoWeather.isNotEmpty())
        assertTrue(demoPests.isNotEmpty())
        assertTrue(demoRecs.any { it.source == AIRecommendationEntity.SOURCE_WEATHER_ENGINE })
        assertTrue(demoRecs.any { it.source == AIRecommendationEntity.SOURCE_AI_CHAT })

        // Create a second user and verify complete farm isolation
        val secondUser = repository.signUp(
            name = "Tariq Hasan",
            email = "tariq@farm.test",
            password = "Password123",
            phone = "+8801900000000",
            farmName = "Riverbank Plot",
            location = "Rajshahi",
            totalLandHa = 2.0,
            soilType = "Clay Loam"
        ).getOrThrow()

        val secondFarm = db.cultiveqDao().getFarmByUserId(secondUser.userId)!!
        assertEquals(0, repository.observeExpenses(secondFarm.farmId).first().size)
        assertEquals(0, repository.observeActivities(secondFarm.farmId).first().size)

        // Add activity and toggle completion -> completed_at populated
        repository.addActivity(secondFarm.farmId, "Irrigate plot 1", System.currentTimeMillis())
        val createdActivity = repository.observeActivities(secondFarm.farmId).first().single()
        assertEquals(ActivityEntity.STATUS_PENDING, createdActivity.status)
        repository.toggleActivityStatus(createdActivity)
        val updatedActivity = repository.observeActivities(secondFarm.farmId).first().single()
        assertEquals(ActivityEntity.STATUS_COMPLETED, updatedActivity.status)
        assertNotNull(updatedActivity.completedAt)
    }
}
