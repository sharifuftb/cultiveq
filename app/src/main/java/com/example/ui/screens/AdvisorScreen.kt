package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.db.AIRecommendationEntity
import com.example.data.db.FarmInformationEntity
import com.example.data.db.WeatherHistoryEntity
import com.example.ui.components.formatDateTime
import com.example.ui.viewmodel.AiOpState

@Composable
fun AdvisorScreen(
    subTabIndex: Int,
    onSelectSubTab: (Int) -> Unit,
    farm: FarmInformationEntity?,
    weatherHistory: List<WeatherHistoryEntity>,
    recommendations: List<AIRecommendationEntity>,
    weatherEngineState: AiOpState,
    weatherSourceLabel: String?,
    chatState: AiOpState,
    pendingChatQuestion: String?,
    onRunWeatherEngine: () -> Unit,
    onSendChat: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 880.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.size(4.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = subTabIndex == 0,
                    onClick = { onSelectSubTab(0) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.testTag("advisor_tab_weather")
                ) {
                    Text("Weather Engine (CR-05)")
                }
                SegmentedButton(
                    selected = subTabIndex == 1,
                    onClick = { onSelectSubTab(1) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.testTag("advisor_tab_chat")
                ) {
                    Text("AI Farm Chat (CR-06)")
                }
            }

            if (subTabIndex == 0) {
                WeatherEnginePane(
                    farm = farm,
                    weatherHistory = weatherHistory,
                    weatherRecommendations = recommendations.filter {
                        it.source == AIRecommendationEntity.SOURCE_WEATHER_ENGINE
                    },
                    engineState = weatherEngineState,
                    weatherSourceLabel = weatherSourceLabel,
                    onRunWeatherEngine = onRunWeatherEngine
                )
            } else {
                AiFarmChatPane(
                    farm = farm,
                    chatRecommendations = recommendations.filter {
                        it.source == AIRecommendationEntity.SOURCE_AI_CHAT
                    },
                    chatState = chatState,
                    pendingQuestion = pendingChatQuestion,
                    onSendChat = onSendChat
                )
            }
        }
    }
}

@Composable
private fun WeatherEnginePane(
    farm: FarmInformationEntity?,
    weatherHistory: List<WeatherHistoryEntity>,
    weatherRecommendations: List<AIRecommendationEntity>,
    engineState: AiOpState,
    weatherSourceLabel: String?,
    onRunWeatherEngine: () -> Unit
) {
    val latest = weatherHistory.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = farm?.location ?: "Farm Location",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = weatherSourceLabel ?: "Snapshot saved to WeatherHistory & AIRecommendations",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Icon(
                            Icons.Filled.WbSunny,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    if (latest != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            WeatherMetricPill("Temp", "${latest.temperature}°C", Modifier.weight(1f))
                            WeatherMetricPill("Humidity", "${latest.humidity}%", Modifier.weight(1f))
                            WeatherMetricPill("Rain", "${latest.rainChance}%", Modifier.weight(1f))
                        }
                        Text(
                            text = "Condition: ${latest.condition} • Recorded ${formatDateTime(latest.recordDate)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        onClick = onRunWeatherEngine,
                        enabled = engineState !is AiOpState.Loading,
                        modifier = Modifier.fillMaxWidth().testTag("run_weather_engine_button")
                    ) {
                        if (engineState is AiOpState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Fetching Weather & Generating AI Advice...")
                        } else {
                            Icon(Icons.Filled.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Fetch Weather & Generate AI Advice")
                        }
                    }

                    if (engineState is AiOpState.Error) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = engineState.message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Weather Engine Advice Log (${weatherRecommendations.size})",
                style = MaterialTheme.typography.titleMedium
            )
        }

        items(weatherRecommendations, key = { it.recId }) { rec ->
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = rec.queryText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = rec.responseText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatDateTime(rec.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                text = "WeatherHistory Snapshots (${weatherHistory.size})",
                style = MaterialTheme.typography.titleMedium
            )
        }

        items(weatherHistory, key = { it.weatherId }) { w ->
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${w.condition} • ${w.temperature}°C",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = formatDateTime(w.recordDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.WaterDrop,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "${w.humidity}% hum / ${w.rainChance}% rain",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.size(16.dp)) }
    }
}

@Composable
private fun WeatherMetricPill(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiFarmChatPane(
    farm: FarmInformationEntity?,
    chatRecommendations: List<AIRecommendationEntity>,
    chatState: AiOpState,
    pendingQuestion: String?,
    onSendChat: (String) -> Unit
) {
    var inputText by rememberSaveable { mutableStateOf("") }
    val quickPrompts = listOf(
        "Best fertilizer schedule for ${farm?.soilType ?: "my soil"}?",
        "Watering plan for ${farm?.totalLandHa ?: 2.0} ha this week?",
        "Organic pest prevention tips for humid weather?"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Grounded in: ${farm?.farmName ?: "Farm"} • ${farm?.location ?: ""} • ${farm?.totalLandHa ?: 0.0} ha • ${farm?.soilType ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            quickPrompts.forEach { prompt ->
                AssistChip(
                    onClick = { onSendChat(prompt) },
                    label = { Text(prompt, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (pendingQuestion != null && (chatState is AiOpState.Loading || chatState is AiOpState.Streaming)) {
                item {
                    ChatPairCards(
                        question = pendingQuestion,
                        answer = if (chatState is AiOpState.Streaming) chatState.partialText else "Thinking with your farm context...",
                        timestampText = "Streaming live..."
                    )
                }
            }

            if (chatState is AiOpState.Error) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = chatState.message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            items(chatRecommendations, key = { it.recId }) { item ->
                ChatPairCards(
                    question = item.queryText,
                    answer = item.responseText,
                    timestampText = formatDateTime(item.createdAt)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Ask Cultiveq AI about your crops or soil...") },
                modifier = Modifier.weight(1f).testTag("ai_chat_input"),
                maxLines = 3
            )
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val q = inputText
                        inputText = ""
                        onSendChat(q)
                    }
                },
                enabled = chatState !is AiOpState.Loading && chatState !is AiOpState.Streaming && inputText.isNotBlank(),
                modifier = Modifier.testTag("ai_chat_send_button")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send question",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ChatPairCards(
    question: String,
    answer: String,
    timestampText: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Surface(
                shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.widthIn(max = 520.dp)
            ) {
                Text(
                    text = question,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Card(
                shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.widthIn(max = 560.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = answer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = timestampText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
