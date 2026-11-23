package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Schema
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.db.AIRecommendationEntity
import com.example.data.db.ActivityEntity
import com.example.data.db.ExpenseEntity
import com.example.data.db.FarmInformationEntity
import com.example.data.db.PestReportEntity
import com.example.data.db.SqliteSchemaTable
import com.example.data.db.UserEntity
import com.example.data.db.WeatherHistoryEntity
import com.example.ui.components.formatDateTime
import com.example.ui.theme.JetBrainsMonoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchemaInspectorDialog(
    user: UserEntity,
    farm: FarmInformationEntity?,
    activities: List<ActivityEntity>,
    expenses: List<ExpenseEntity>,
    pestReports: List<PestReportEntity>,
    weatherHistory: List<WeatherHistoryEntity>,
    recommendations: List<AIRecommendationEntity>,
    schemaDdl: List<SqliteSchemaTable>,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    var selectedTab by rememberSaveable { mutableIntStateOf(0) } // 0 = ER Diagram, 1 = Live Records, 2 = SQL DDL

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Schema, contentDescription = null)
                            Text("3NF Schema & ER Inspector")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_schema_inspector_button")
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Close inspector")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                        modifier = Modifier.testTag("inspector_tab_er")
                    ) {
                        Text("ER Map")
                    }
                    SegmentedButton(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                        modifier = Modifier.testTag("inspector_tab_records")
                    ) {
                        Text("Live Rows")
                    }
                    SegmentedButton(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                        modifier = Modifier.testTag("inspector_tab_sql")
                    ) {
                        Text("SQL DDL")
                    }
                }

                when (selectedTab) {
                    0 -> ErDiagramTab(
                        user = user,
                        farm = farm,
                        activitiesCount = activities.size,
                        expensesCount = expenses.size,
                        pestCount = pestReports.size,
                        weatherCount = weatherHistory.size,
                        aiCount = recommendations.size
                    )
                    1 -> LiveRecordsTab(
                        user = user,
                        farm = farm,
                        activities = activities,
                        expenses = expenses,
                        pestReports = pestReports,
                        weatherHistory = weatherHistory,
                        recommendations = recommendations
                    )
                    else -> SqlDdlTab(schemaDdl = schemaDdl)
                }
            }
        }
    }
}

@Composable
private fun ErDiagramTab(
    user: UserEntity,
    farm: FarmInformationEntity?,
    activitiesCount: Int,
    expensesCount: Int,
    pestCount: Int,
    weatherCount: Int,
    aiCount: Int
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            EntityNodeCard(
                tableName = "1. users",
                cardinality = "ROOT ENTITY",
                rowCount = 1,
                keysText = "PK: user_id (${user.userId}) • UNIQUE: email",
                columns = "user_id, name, email, password_hash, phone, created_at"
            )
        }
        item { RelationshipConnector("1 : 1  (FK: users.user_id → farm_information.user_id UNIQUE)") }
        item {
            EntityNodeCard(
                tableName = "2. farm_information",
                cardinality = "1:1 WITH USERS",
                rowCount = if (farm != null) 1 else 0,
                keysText = "PK: farm_id (${farm?.farmId ?: "-"}) • FK: user_id (${user.userId})",
                columns = "farm_id, user_id, farm_name, location, total_land_ha, soil_type, created_at"
            )
        }
        item { RelationshipConnector("1 : N  (FK: farm_information.farm_id → child tables CASCADE)") }
        item {
            EntityNodeCard(
                tableName = "3. activities",
                cardinality = "1:N FARM CHILD",
                rowCount = activitiesCount,
                keysText = "PK: activity_id • FK: farm_id (${farm?.farmId ?: "-"})",
                columns = "activity_id, farm_id, title, scheduled_at, status, completed_at, created_at"
            )
        }
        item {
            EntityNodeCard(
                tableName = "4. expenses",
                cardinality = "1:N FARM CHILD",
                rowCount = expensesCount,
                keysText = "PK: expense_id • FK: farm_id (${farm?.farmId ?: "-"})",
                columns = "expense_id, farm_id, category, amount, expense_date, note"
            )
        }
        item {
            EntityNodeCard(
                tableName = "5. pest_reports",
                cardinality = "1:N FARM CHILD",
                rowCount = pestCount,
                keysText = "PK: report_id • FK: farm_id (${farm?.farmId ?: "-"})",
                columns = "report_id, farm_id, image_ref, detected_issue, recommendation, reported_at"
            )
        }
        item {
            EntityNodeCard(
                tableName = "6. weather_history",
                cardinality = "1:N FARM CHILD",
                rowCount = weatherCount,
                keysText = "PK: weather_id • FK: farm_id (${farm?.farmId ?: "-"})",
                columns = "weather_id, farm_id, record_date, temperature, humidity, rain_chance, condition"
            )
        }
        item {
            EntityNodeCard(
                tableName = "7. ai_recommendations",
                cardinality = "1:N FARM CHILD",
                rowCount = aiCount,
                keysText = "PK: rec_id • FK: farm_id (${farm?.farmId ?: "-"})",
                columns = "rec_id, farm_id, source, query_text, response_text, created_at"
            )
        }
        item { Spacer(modifier = Modifier.size(16.dp)) }
    }
}

@Composable
private fun RelationshipConnector(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Filled.ArrowDownward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = JetBrainsMonoFontFamily,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun EntityNodeCard(
    tableName: String,
    cardinality: String,
    rowCount: Int,
    keysText: String,
    columns: String
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tableName,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$cardinality • $rowCount rows",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Filled.Key,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = keysText,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = JetBrainsMonoFontFamily,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Text(
                text = columns,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = JetBrainsMonoFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LiveRecordsTab(
    user: UserEntity,
    farm: FarmInformationEntity?,
    activities: List<ActivityEntity>,
    expenses: List<ExpenseEntity>,
    pestReports: List<PestReportEntity>,
    weatherHistory: List<WeatherHistoryEntity>,
    recommendations: List<AIRecommendationEntity>
) {
    val tables = listOf(
        "users",
        "farm_information",
        "activities",
        "expenses",
        "pest_reports",
        "weather_history",
        "ai_recommendations"
    )
    var selectedTable by rememberSaveable { mutableStateOf("users") }

    val rowStrings: List<String> = when (selectedTable) {
        "users" -> listOf(
            "user_id=${user.userId} | name=${user.name} | email=${user.email} | phone=${user.phone} | password_hash=${user.passwordHash.take(18)}... | created_at=${formatDateTime(user.createdAt)}"
        )
        "farm_information" -> if (farm == null) emptyList() else listOf(
            "farm_id=${farm.farmId} | user_id(FK)=${farm.userId} | farm_name=${farm.farmName} | location=${farm.location} | total_land_ha=${farm.totalLandHa} | soil_type=${farm.soilType}"
        )
        "activities" -> activities.map {
            "activity_id=${it.activityId} | farm_id(FK)=${it.farmId} | title=${it.title} | status=${it.status} | scheduled=${formatDateTime(it.scheduledAt)} | completed_at=${it.completedAt?.let(::formatDateTime) ?: "NULL"}"
        }
        "expenses" -> expenses.map {
            "expense_id=${it.expenseId} | farm_id(FK)=${it.farmId} | category=${it.category} | amount=${it.amount} | date=${formatDateTime(it.expenseDate)} | note=${it.note}"
        }
        "pest_reports" -> pestReports.map {
            "report_id=${it.reportId} | farm_id(FK)=${it.farmId} | detected_issue=${it.detectedIssue} | image_ref=${it.imageRef.take(30)} | reported_at=${formatDateTime(it.reportedAt)}"
        }
        "weather_history" -> weatherHistory.map {
            "weather_id=${it.weatherId} | farm_id(FK)=${it.farmId} | temp=${it.temperature}°C | humidity=${it.humidity}% | rain_chance=${it.rainChance}% | condition=${it.condition}"
        }
        else -> recommendations.map {
            "rec_id=${it.recId} | farm_id(FK)=${it.farmId} | source=${it.source} | query=${it.queryText.take(60)} | created_at=${formatDateTime(it.createdAt)}"
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tables.forEach { t ->
                FilterChip(
                    selected = selectedTable == t,
                    onClick = { selectedTable = t },
                    label = { Text(t, fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (rowStrings.isEmpty()) {
                item {
                    Text(
                        text = "0 scoped rows currently stored in $selectedTable.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(rowStrings) { row ->
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = row,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = JetBrainsMonoFontFamily,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SqlDdlTab(schemaDdl: List<SqliteSchemaTable>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Live CREATE TABLE statements from SQLite sqlite_master (${schemaDdl.size} tables):",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(schemaDdl, key = { it.name }) { table ->
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = table.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = JetBrainsMonoFontFamily,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${table.sql};",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = JetBrainsMonoFontFamily,
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.size(16.dp)) }
    }
}
