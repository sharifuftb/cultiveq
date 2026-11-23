package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.db.ActivityEntity
import com.example.ui.components.formatDateTime
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusPending

@Composable
fun ActivitiesScreen(
    activities: List<ActivityEntity>,
    onAddActivity: (title: String, scheduledAt: Long) -> Unit,
    onToggleActivity: (ActivityEntity) -> Unit,
    onDeleteActivity: (Long) -> Unit
) {
    var filterIndex by rememberSaveable { mutableIntStateOf(0) } // 0 = All, 1 = Pending, 2 = Completed
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    val filtered = when (filterIndex) {
        1 -> activities.filter { it.status == ActivityEntity.STATUS_PENDING }
        2 -> activities.filter { it.status == ActivityEntity.STATUS_COMPLETED }
        else -> activities
    }

    val pendingCount = activities.count { it.status == ActivityEntity.STATUS_PENDING }
    val completedCount = activities.count { it.status == ActivityEntity.STATUS_COMPLETED }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("add_activity_fab"),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add scheduled activity")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 880.dp).fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = "Farm Activities & Schedule",
                    style = MaterialTheme.typography.headlineSmall
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = filterIndex == 0,
                        onClick = { filterIndex = 0 },
                        label = { Text("All (${activities.size})") },
                        modifier = Modifier.testTag("filter_all_tasks")
                    )
                    FilterChip(
                        selected = filterIndex == 1,
                        onClick = { filterIndex = 1 },
                        label = { Text("Pending ($pendingCount)") },
                        modifier = Modifier.testTag("filter_pending_tasks")
                    )
                    FilterChip(
                        selected = filterIndex == 2,
                        onClick = { filterIndex = 2 },
                        label = { Text("Completed ($completedCount)") },
                        modifier = Modifier.testTag("filter_completed_tasks")
                    )
                }

                if (filtered.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.EventAvailable,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "No activities in this filter",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Tap + to schedule irrigation, fertilization, scouting, or harvest tasks.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filtered, key = { it.activityId }) { item ->
                            val isDone = item.status == ActivityEntity.STATUS_COMPLETED
                            Card(
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    IconButton(
                                        onClick = { onToggleActivity(item) },
                                        modifier = Modifier.testTag("toggle_activity_${item.activityId}")
                                    ) {
                                        Icon(
                                            imageVector = if (isDone) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                            contentDescription = if (isDone) "Mark pending" else "Mark completed",
                                            tint = if (isDone) StatusCompleted else StatusPending,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Schedule,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Scheduled: ${formatDateTime(item.scheduledAt)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (isDone && item.completedAt != null) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = StatusCompleted.copy(alpha = 0.14f)
                                            ) {
                                                Text(
                                                    text = "Completed: ${formatDateTime(item.completedAt)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = StatusCompleted,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }

                                    IconButton(
                                        onClick = { onDeleteActivity(item.activityId) }
                                    ) {
                                        Icon(
                                            Icons.Filled.DeleteOutline,
                                            contentDescription = "Delete activity",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.size(72.dp)) }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddActivityDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, scheduledAt ->
                onAddActivity(title, scheduledAt)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddActivityDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, scheduledAt: Long) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var selectedOffsetHours by rememberSaveable { mutableLongStateOf(4L) }

    val presets = listOf(
        "In 4 hrs" to 4L,
        "Tomorrow AM" to 18L,
        "In 2 days" to 48L,
        "Next week" to 168L
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Farm Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title (e.g. Drip check Plot A)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_activity_title_input")
                )
                Text(
                    text = "Schedule Window",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.take(2).forEach { (label, hrs) ->
                        FilterChip(
                            selected = selectedOffsetHours == hrs,
                            onClick = { selectedOffsetHours = hrs },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.drop(2).forEach { (label, hrs) ->
                        FilterChip(
                            selected = selectedOffsetHours == hrs,
                            onClick = { selectedOffsetHours = hrs },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val scheduledAt = System.currentTimeMillis() + selectedOffsetHours * 3_600_000L
                    onConfirm(title, scheduledAt)
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_activity_button")
            ) {
                Text("Schedule")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
