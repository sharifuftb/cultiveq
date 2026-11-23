package com.example.data.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.data.db.FarmInformationEntity
import com.example.data.db.WeatherHistoryEntity
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "CultiveqAiService"
private const val MODEL_NAME = "gemini-3.8-flash"
private const val AI_TIMEOUT_MS = 60_000L

data class StructuredPestDiagnosis(
    val detectedIssue: String,
    val severity: String,
    val confidencePercent: Int,
    val immediateAction: String,
    val organicTreatment: String,
    val preventiveMeasure: String
) {
    fun toFormattedRecommendation(): String = buildString {
        appendLine("Severity: $severity ($confidencePercent% match)")
        appendLine("Immediate Action: $immediateAction")
        appendLine("Organic / Targeted Treatment: $organicTreatment")
        append("Prevention: $preventiveMeasure")
    }
}

class CultiveqAiService {

    private val generalModel by lazy {
        Firebase.ai.generativeModel(MODEL_NAME)
    }

    private val pestDiagnosisSchema = Schema.obj(
        properties = mapOf(
            "detectedIssue" to Schema.string(description = "Primary pest, fungal, or nutrient deficiency diagnosis"),
            "severity" to Schema.enumeration(
                values = listOf("Low", "Moderate", "High", "Critical"),
                description = "Estimated infestation or crop stress severity"
            ),
            "confidencePercent" to Schema.integer(description = "Diagnostic confidence percentage between 40 and 99"),
            "immediateAction" to Schema.string(description = "Actionable step to take within 24 hours"),
            "organicTreatment" to Schema.string(description = "Targeted organic or safe agronomic treatment"),
            "preventiveMeasure" to Schema.string(description = "Long-term soil or crop rotation safeguard")
        )
    )

    private val pestModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = pestDiagnosisSchema
            }
        )
    }

    suspend fun generateWeatherAdvice(
        farm: FarmInformationEntity,
        weather: WeatherHistoryEntity
    ): Result<String> {
        val prompt = """
            You are Cultiveq V2, a precision agronomy advisor.
            Provide concise, practical field advice for today based on this farm and weather snapshot:
            - Farm Name: ${farm.farmName}
            - Location: ${farm.location}
            - Land Size: ${farm.totalLandHa} hectares
            - Soil Type: ${farm.soilType}
            - Temperature: ${weather.temperature}°C
            - Humidity: ${weather.humidity}%
            - Rain Probability: ${weather.rainChance}%
            - Sky Condition: ${weather.condition}

            Return 3 short bullet points covering:
            1) Irrigation & soil moisture timing for ${farm.soilType} soil
            2) Spraying / fertilization window given ${weather.rainChance}% rain chance
            3) Crop protection watchout for ${weather.humidity}% humidity.
            Keep the total response under 110 words.
        """.trimIndent()

        return try {
            val response = withTimeout(AI_TIMEOUT_MS) {
                generalModel.generateContent(prompt)
            }
            val text = response.text?.trim()
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.failure(IllegalStateException("Gemini returned an empty weather recommendation."))
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Weather advice generation timed out", e)
            Result.failure(IllegalStateException("Weather advisory timed out after 60s. Please retry."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Weather advice generation failed", e)
            Result.failure(e)
        }
    }

    fun streamFarmChatResponse(
        question: String,
        farm: FarmInformationEntity,
        latestWeather: WeatherHistoryEntity?
    ): Flow<String> = flow {
        val weatherLine = if (latestWeather != null) {
            "Latest Farm Weather: ${latestWeather.temperature}°C, ${latestWeather.humidity}% humidity, ${latestWeather.rainChance}% rain chance (${latestWeather.condition})."
        } else {
            "Latest Farm Weather: No recent snapshot recorded."
        }

        val prompt = """
            You are Cultiveq V2, an expert AI agronomist assisting a specific farmer.
            Always tailor your guidance to the farmer's stored profile:
            - Farm: ${farm.farmName}
            - Location: ${farm.location}
            - Total Land: ${farm.totalLandHa} hectares
            - Soil Type: ${farm.soilType}
            - $weatherLine

            Farmer Question: $question

            Give clear, actionable, practical guidance referencing their soil type, acreage, or local conditions where helpful.
        """.trimIndent()

        val accumulated = StringBuilder()
        withTimeout(AI_TIMEOUT_MS) {
            generalModel.generateContentStream(prompt).collect { chunk ->
                val part = chunk.text.orEmpty()
                if (part.isNotEmpty()) {
                    accumulated.append(part)
                    emit(accumulated.toString())
                }
            }
        }
        if (accumulated.isBlank()) {
            throw IllegalStateException("AI assistant returned an empty response.")
        }
    }

    suspend fun analyzePest(
        farm: FarmInformationEntity,
        symptoms: List<String>,
        notes: String,
        cropBitmap: Bitmap?
    ): Result<StructuredPestDiagnosis> {
        val promptText = """
            Analyze this crop health report for ${farm.farmName} (${farm.location}, ${farm.soilType} soil, ${farm.totalLandHa} ha).
            Reported Symptoms: ${if (symptoms.isEmpty()) "None selected (rely on visual inspection)" else symptoms.joinToString(", ")}
            Additional Farmer Notes: ${notes.ifBlank { "None" }}

            Identify the most likely pest, disease, or nutrient deficiency and provide structured treatment guidance.
        """.trimIndent()

        return try {
            val response = withTimeout(AI_TIMEOUT_MS) {
                if (cropBitmap != null) {
                    pestModel.generateContent(
                        content {
                            image(cropBitmap)
                            text(promptText)
                        }
                    )
                } else {
                    pestModel.generateContent(promptText)
                }
            }
            val rawJson = response.text
                ?: return Result.failure(IllegalStateException("Gemini returned empty pest diagnosis JSON."))
            val json = JSONObject(rawJson)
            Result.success(
                StructuredPestDiagnosis(
                    detectedIssue = json.getString("detectedIssue"),
                    severity = json.optString("severity", "Moderate"),
                    confidencePercent = json.optInt("confidencePercent", 82),
                    immediateAction = json.getString("immediateAction"),
                    organicTreatment = json.getString("organicTreatment"),
                    preventiveMeasure = json.getString("preventiveMeasure")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Pest diagnosis timed out", e)
            Result.failure(IllegalStateException("Pest analysis timed out after 60s. Please retry."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Pest diagnosis failed", e)
            Result.failure(e)
        }
    }
}
