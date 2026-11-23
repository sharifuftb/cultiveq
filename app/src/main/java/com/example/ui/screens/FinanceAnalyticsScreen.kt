package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Payments
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.db.ActivityEntity
import com.example.data.db.ExpenseEntity
import com.example.ui.components.ActivityCompletionGauge
import com.example.ui.components.ExpenseCategoryBarChart
import com.example.ui.components.categoryColor
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDateShort

@Composable
fun FinanceAnalyticsScreen(
    expenses: List<ExpenseEntity>,
    activities: List<ActivityEntity>,
    onAddExpense: (category: String, amountText: String, expenseDate: Long, note: String) -> Unit,
    onDeleteExpense: (Long) -> Unit
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var selectedCategoryFilter by rememberSaveable { mutableStateOf("All") }

    val totalSpend = expenses.sumOf { it.amount }
    val categoryTotals = ExpenseEntity.CATEGORIES.associateWith { cat ->
        expenses.filter { it.category == cat }.sumOf { it.amount }
    }
    val completedTasks = activities.count { it.status == ActivityEntity.STATUS_COMPLETED }
    val filteredExpenses = if (selectedCategoryFilter == "All") {
        expenses
    } else {
        expenses.filter { it.category == selectedCategoryFilter }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("add_expense_fab"),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Log farm expense")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.size(4.dp))
                Column(
                    modifier = Modifier.widthIn(max = 880.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Expenses & Farm Analytics (CR-08 & CR-09)",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    // Total Spend Hero Card
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Total Season Spend",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = formatCurrency(totalSpend),
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${expenses.size} entries across ${categoryTotals.count { it.value > 0 }} active categories",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Icon(
                                Icons.Filled.Payments,
                                contentDescription = null,
                                modifier = Modifier.size(38.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Category Breakdown Chart Card
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Expenses by Category",
                                style = MaterialTheme.typography.titleMedium
                            )
                            ExpenseCategoryBarChart(categoryTotals = categoryTotals)
                        }
                    }

                    // Activity Completion Analytics Card
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(modifier = Modifier.padding(18.dp)) {
                            ActivityCompletionGauge(
                                completedCount = completedTasks,
                                totalCount = activities.size
                            )
                        }
                    }

                    // Category Filter Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("All", "Seeds", "Fertilizer", "Labour").forEach { cat ->
                            FilterChip(
                                selected = selectedCategoryFilter == cat,
                                onClick = { selectedCategoryFilter = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Irrigation", "Other").forEach { cat ->
                            FilterChip(
                                selected = selectedCategoryFilter == cat,
                                onClick = { selectedCategoryFilter = cat },
                                label = { Text(cat) }
                            )
                        }
                    }

                    Text(
                        text = "Expense Ledger (${filteredExpenses.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            items(filteredExpenses, key = { it.expenseId }) { exp ->
                Card(
                    modifier = Modifier.widthIn(max = 880.dp).fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(categoryColor(exp.category))
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = exp.category,
                                style = MaterialTheme.typography.titleSmall
                            )
                            if (exp.note.isNotBlank()) {
                                Text(
                                    text = exp.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = formatDateShort(exp.expenseDate),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = formatCurrency(exp.amount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { onDeleteExpense(exp.expenseId) }) {
                            Icon(
                                Icons.Filled.DeleteOutline,
                                contentDescription = "Delete expense",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.size(72.dp)) }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { category, amountText, note ->
                onAddExpense(category, amountText, System.currentTimeMillis(), note)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (category: String, amountText: String, note: String) -> Unit
) {
    var category by rememberSaveable { mutableStateOf(ExpenseEntity.CATEGORIES.first()) }
    var amountText by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Farm Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExpenseEntity.CATEGORIES.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_expense_amount_input")
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (e.g. Organic compost bags)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_expense_note_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(category, amountText, note) },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0.0,
                modifier = Modifier.testTag("confirm_add_expense_button")
            ) {
                Text("Save Expense")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
