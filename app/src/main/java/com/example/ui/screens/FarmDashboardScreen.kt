package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.db.AIRecommendationEntity
import com.example.data.db.ActivityEntity
import com.example.data.db.ExpenseEntity
import com.example.data.db.FarmInformationEntity
import com.example.data.db.PestReportEntity
import com.example.data.db.UserEntity
import com.example.data.db.WeatherHistoryEntity
import com.example.ui.components.SOIL_TYPES
import com.example.ui.components.formatCurrency
import com.example.ui.viewmodel.CultiveqTab

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FarmDashboardScreen(
    user: UserEntity,
    farm: FarmInformationEntity?,
    activities: List<ActivityEntity>,
    expenses: List<ExpenseEntity>,
    pestReports: List<PestReportEntity>,
    weatherHistory: List<WeatherHistoryEntity>,
    recommendations: List<AIRecommendationEntity>,
    onSaveFarmProfile: (name: String, location: String, landHa: String, soilType: String) -> Unit,
    onNavigateTab: (CultiveqTab) -> Unit,
    onOpenWeatherEngine: () -> Unit,
    onOpenAiChat: () -> Unit,
    onOpenSchemaInspector: () -> Unit
) {
    var editingProfile by rememberSaveable { mutableStateOf(farm == null) }
    var farmName by rememberSaveable { mutableStateOf(farm?.farmName.orEmpty()) }
    var location by rememberSaveable { mutableStateOf(farm?.location.orEmpty()) }
    var totalLandHa by rememberSaveable { mutableStateOf(farm?.totalLandHa?.toString() ?: "2.5") }
    var soilType by rememberSaveable { mutableStateOf(farm?.soilType ?: SOIL_TYPES.first()) }

    LaunchedEffect(farm) {
        if (farm != null) {
            farmName = farm.farmName
            location = farm.location
            totalLandHa = farm.totalLandHa.toString()
            soilType = farm.soilType
        }
    }

    val pendingTasks = activities.count { it.status == ActivityEntity.STATUS_PENDING }
    val totalSpend = expenses.sumOf { it.amount }
    val latestWeather = weatherHistory.firstOrNull()
    val latestRec = recommendations.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 880.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Personalized Farm Hero Overview Card
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_farm_hero),
                        contentDescription = "Farm landscape",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.25f),
                                        Color(0xFF0A2415).copy(alpha = 0.90f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "Farmer: ${user.name}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                            OutlinedButton(
                                onClick = { editingProfile = !editingProfile },
                                modifier = Modifier.testTag("toggle_edit_farm_button"),
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Edit farm profile",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(
                                    text = if (editingProfile) "Close" else "Edit Farm",
                                    color = Color.White
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = farm?.farmName ?: "Set Up Your Farm Profile",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                            if (farm != null) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.LocationOn,
                                            contentDescription = null,
                                            tint = Color(0xFFB4F0C2),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.size(4.dp))
                                        Text(
                                            text = farm.location,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.92f)
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.Landscape,
                                            contentDescription = null,
                                            tint = Color(0xFFB4F0C2),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.size(4.dp))
                                        Text(
                                            text = "${farm.totalLandHa} ha • ${farm.soilType}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.92f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Create / Edit Farm Profile Form (CR-03)
            AnimatedVisibility(visible = editingProfile) {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "FarmInformation Record (1:1 with ${user.name})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        OutlinedTextField(
                            value = farmName,
                            onValueChange = { farmName = it },
                            label = { Text("Farm Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("farm_name_input")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = location,
                                onValueChange = { location = it },
                                label = { Text("Location") },
                                singleLine = true,
                                modifier = Modifier.weight(1.4f).testTag("farm_location_input")
                            )
                            OutlinedTextField(
                                value = totalLandHa,
                                onValueChange = { totalLandHa = it },
                                label = { Text("Total Land (ha)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(0.9f).testTag("farm_land_input")
                            )
                        }
                        Text(
                            text = "Soil Type",
                            style = MaterialTheme.typography.labelLarge
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SOIL_TYPES.forEach { option ->
                                FilterChip(
                                    selected = soilType == option,
                                    onClick = { soilType = option },
                                    label = { Text(option) }
                                )
                            }
                        }
                        Button(
                            onClick = {
                                onSaveFarmProfile(farmName, location, totalLandHa, soilType)
                                editingProfile = false
                            },
                            modifier = Modifier.fillMaxWidth().testTag("save_farm_profile_button")
                        ) {
                            Text("Save Farm Profile")
                        }
                    }
                }
            }

            // KPI Metrics Grid (2x2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "Pending Tasks",
                    value = "$pendingTasks",
                    subtitle = "${activities.size} total logged",
                    icon = Icons.Filled.CheckCircle,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    onClick = { onNavigateTab(CultiveqTab.TASKS) },
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Season Spend",
                    value = formatCurrency(totalSpend),
                    subtitle = "${expenses.size} entries",
                    icon = Icons.Filled.AccountBalanceWallet,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = { onNavigateTab(CultiveqTab.FINANCE) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "Latest Weather",
                    value = latestWeather?.let { "${it.temperature}°C" } ?: "--°C",
                    subtitle = latestWeather?.condition ?: "Tap to fetch snapshot",
                    icon = Icons.Filled.WbSunny,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    onClick = onOpenWeatherEngine,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Pest Reports",
                    value = "${pestReports.size}",
                    subtitle = pestReports.firstOrNull()?.detectedIssue?.take(24) ?: "No issues logged",
                    icon = Icons.Filled.BugReport,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = { onNavigateTab(CultiveqTab.PEST) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Latest AI Agronomy Insight Card
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Agriculture,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Latest AI Agronomy Insight",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        if (latestRec != null) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = if (latestRec.source == AIRecommendationEntity.SOURCE_WEATHER_ENGINE) "Weather Engine" else "AI Chat",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (latestRec != null) {
                        Text(
                            text = latestRec.responseText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Run the Smart Weather Engine or ask the AI Farming Assistant to generate personalized guidance for ${farm?.farmName ?: "your farm"}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenWeatherEngine,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.WbSunny, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Weather Advice")
                        }
                        Button(
                            onClick = onOpenAiChat,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Ask AI Advisor")
                        }
                    }
                }
            }

            // Database Architecture Quick Review Card
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSchemaInspector() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Filled.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Column {
                            Text(
                                text = "Database Schema & ER Inspector",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Inspect 7 normalized 3NF tables, FK links & live SQLite DDL",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    OutlinedButton(onClick = onOpenSchemaInspector) {
                        Text("Inspect")
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
    }
}
