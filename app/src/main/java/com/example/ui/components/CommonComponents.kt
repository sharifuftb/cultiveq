package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ChartFertilizer
import com.example.ui.theme.ChartIrrigation
import com.example.ui.theme.ChartLabour
import com.example.ui.theme.ChartOther
import com.example.ui.theme.ChartSeeds
import com.example.ui.theme.StatusCompleted
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDateShort(millis: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

fun formatDateTime(millis: Long): String {
    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}

fun formatCurrency(amount: Double): String {
    val fmt = NumberFormat.getCurrencyInstance(Locale.US)
    return fmt.format(amount)
}

fun categoryColor(category: String): Color = when (category) {
    "Seeds" -> ChartSeeds
    "Fertilizer" -> ChartFertilizer
    "Labour" -> ChartLabour
    "Irrigation" -> ChartIrrigation
    else -> ChartOther
}

val SOIL_TYPES = listOf(
    "Loamy Alluvial",
    "Clay Loam",
    "Sandy Loam",
    "Silty Clay",
    "Peaty",
    "Chalky"
)

@Composable
fun VersionBadgeChip(
    text: String = "v2.0 — Database-Driven Release",
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Verified,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ExpenseCategoryBarChart(
    categoryTotals: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val maxVal = (categoryTotals.values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    val totalSpend = categoryTotals.values.sum().coerceAtLeast(0.01)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categoryTotals.entries.forEach { (category, amount) ->
            val fraction = (amount / maxVal).toFloat().coerceIn(0f, 1f)
            val sharePct = ((amount / totalSpend) * 100).toInt()
            val barColor = categoryColor(category)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(barColor)
                        )
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Text(
                        text = "${formatCurrency(amount)} ($sharePct%)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = if (amount > 0) fraction.coerceAtLeast(0.04f) else 0f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(barColor)
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityCompletionGauge(
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val rate = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()
    val pct = (rate * 100).toInt()
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(86.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(86.dp)) {
                val strokeWidth = 10.dp.toPx()
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = StatusCompleted,
                    startAngle = -90f,
                    sweepAngle = 360f * rate,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Text(
                text = "$pct%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Task Completion Rate",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "$completedCount of $totalCount scheduled activities completed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(StatusCompleted)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$completedCount Done",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(trackColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${totalCount - completedCount} Pending",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
