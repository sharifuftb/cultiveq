package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.VersionBadgeChip
import com.example.ui.screens.ActivitiesScreen
import com.example.ui.screens.AdvisorScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.FarmDashboardScreen
import com.example.ui.screens.FinanceAnalyticsScreen
import com.example.ui.screens.PestDetectionScreen
import com.example.ui.screens.SchemaInspectorDialog
import com.example.ui.theme.CultiveqTheme
import com.example.ui.viewmodel.AuthState
import com.example.ui.viewmodel.CultiveqTab
import com.example.ui.viewmodel.CultiveqViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Firebase App Check with launch intent before rendering UI or creating ViewModels.
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            CultiveqTheme {
                CultiveqApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultiveqApp(viewModel: CultiveqViewModel = viewModel()) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val authBusy by viewModel.authBusy.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val advisorSubTab by viewModel.advisorSubTab.collectAsStateWithLifecycle()

    val activities by viewModel.activities.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val pestReports by viewModel.pestReports.collectAsStateWithLifecycle()
    val weatherHistory by viewModel.weatherHistory.collectAsStateWithLifecycle()
    val recommendations by viewModel.recommendations.collectAsStateWithLifecycle()

    val weatherEngineState by viewModel.weatherEngineState.collectAsStateWithLifecycle()
    val weatherSourceLabel by viewModel.weatherSourceLabel.collectAsStateWithLifecycle()
    val chatState by viewModel.chatState.collectAsStateWithLifecycle()
    val pendingChatQuestion by viewModel.pendingChatQuestion.collectAsStateWithLifecycle()
    val pestState by viewModel.pestState.collectAsStateWithLifecycle()

    val showSchemaInspector by viewModel.showSchemaInspector.collectAsStateWithLifecycle()
    val schemaDdl by viewModel.schemaDdl.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusBanner) {
        val msg = statusBanner
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusBanner()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = authState) {
            AuthState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            AuthState.Unauthenticated -> {
                AuthScreen(
                    isBusy = authBusy,
                    errorMessage = authError,
                    onLogin = viewModel::login,
                    onSignUp = viewModel::signUp,
                    onLoadDemoFarmer = viewModel::loadDemoFarmer,
                    onClearError = viewModel::clearAuthError
                )
            }

            is AuthState.Authenticated -> {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val useNavRail = maxWidth >= 680.dp

                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            TopAppBar(
                                title = {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Cultiveq V2",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            VersionBadgeChip(text = "v2.0")
                                        }
                                        Text(
                                            text = state.farm?.farmName ?: "Personalize Your Farm",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                actions = {
                                    IconButton(
                                        onClick = viewModel::openSchemaInspector,
                                        modifier = Modifier.testTag("open_schema_inspector_button")
                                    ) {
                                        Icon(
                                            Icons.Filled.Storage,
                                            contentDescription = "Database Schema & ER Inspector",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(
                                        onClick = viewModel::logout,
                                        modifier = Modifier.testTag("logout_button")
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Logout,
                                            contentDescription = "Logout"
                                        )
                                    }
                                }
                            )
                        },
                        bottomBar = {
                            if (!useNavRail) {
                                NavigationBar {
                                    // Note: Scaffold handles navigation bar insets cleanly; ensure bottom padding
                                    CultiveqTab.entries.forEach { tab ->
                                        NavigationBarItem(
                                            selected = selectedTab == tab,
                                            onClick = { viewModel.selectTab(tab) },
                                            icon = { Icon(tabIcon(tab), contentDescription = tab.label) },
                                            label = { Text(tab.label, maxLines = 1) },
                                            modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (useNavRail) {
                                NavigationRail(
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Spacer(modifier = Modifier.size(8.dp))
                                    CultiveqTab.entries.forEach { tab ->
                                        NavigationRailItem(
                                            selected = selectedTab == tab,
                                            onClick = { viewModel.selectTab(tab) },
                                            icon = { Icon(tabIcon(tab), contentDescription = tab.label) },
                                            label = { Text(tab.label) },
                                            modifier = Modifier.testTag("rail_${tab.name.lowercase()}")
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    NavigationRailItem(
                                        selected = showSchemaInspector,
                                        onClick = viewModel::openSchemaInspector,
                                        icon = { Icon(Icons.Filled.Storage, contentDescription = "ER Schema") },
                                        label = { Text("ER Schema") }
                                    )
                                    Spacer(modifier = Modifier.size(12.dp))
                                }
                            }

                            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                                when (selectedTab) {
                                    CultiveqTab.FARM -> FarmDashboardScreen(
                                        user = state.user,
                                        farm = state.farm,
                                        activities = activities,
                                        expenses = expenses,
                                        pestReports = pestReports,
                                        weatherHistory = weatherHistory,
                                        recommendations = recommendations,
                                        onSaveFarmProfile = viewModel::saveFarmProfile,
                                        onNavigateTab = viewModel::selectTab,
                                        onOpenWeatherEngine = viewModel::openWeatherEngine,
                                        onOpenAiChat = viewModel::openAiChat,
                                        onOpenSchemaInspector = viewModel::openSchemaInspector
                                    )

                                    CultiveqTab.TASKS -> ActivitiesScreen(
                                        activities = activities,
                                        onAddActivity = viewModel::addActivity,
                                        onToggleActivity = viewModel::toggleActivity,
                                        onDeleteActivity = viewModel::deleteActivity
                                    )

                                    CultiveqTab.ADVISOR -> AdvisorScreen(
                                        subTabIndex = advisorSubTab,
                                        onSelectSubTab = viewModel::selectAdvisorSubTab,
                                        farm = state.farm,
                                        weatherHistory = weatherHistory,
                                        recommendations = recommendations,
                                        weatherEngineState = weatherEngineState,
                                        weatherSourceLabel = weatherSourceLabel,
                                        chatState = chatState,
                                        pendingChatQuestion = pendingChatQuestion,
                                        onRunWeatherEngine = viewModel::runWeatherRecommendationEngine,
                                        onSendChat = viewModel::sendChatMessage
                                    )

                                    CultiveqTab.PEST -> PestDetectionScreen(
                                        pestReports = pestReports,
                                        pestState = pestState,
                                        onAnalyzePest = viewModel::analyzePestReport
                                    )

                                    CultiveqTab.FINANCE -> FinanceAnalyticsScreen(
                                        expenses = expenses,
                                        activities = activities,
                                        onAddExpense = viewModel::addExpense,
                                        onDeleteExpense = viewModel::deleteExpense
                                    )
                                }
                            }
                        }
                    }

                    if (showSchemaInspector) {
                        SchemaInspectorDialog(
                            user = state.user,
                            farm = state.farm,
                            activities = activities,
                            expenses = expenses,
                            pestReports = pestReports,
                            weatherHistory = weatherHistory,
                            recommendations = recommendations,
                            schemaDdl = schemaDdl,
                            onDismiss = viewModel::closeSchemaInspector
                        )
                    }
                }
            }
        }
    }
}

private fun tabIcon(tab: CultiveqTab) = when (tab) {
    CultiveqTab.FARM -> Icons.Filled.Agriculture
    CultiveqTab.TASKS -> Icons.Filled.CheckCircle
    CultiveqTab.ADVISOR -> Icons.Filled.AutoAwesome
    CultiveqTab.PEST -> Icons.Filled.BugReport
    CultiveqTab.FINANCE -> Icons.Filled.AccountBalanceWallet
}
