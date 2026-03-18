package com.yesser_studios.death_switch

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yesser_studios.death_switch.ui.theme.GoogleSans
import com.yesser_studios.death_switch.ui.theme.Typography
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatisticsScreen(
    repository: StatsRepository,
    modifier: Modifier = Modifier
) {
    val locale = Locale.getDefault()
    
    val database by repository.database.collectAsState(initial = DeathsDatabase())
    var selectedRange by remember { mutableStateOf(TimeRange.WEEK) }
    val records = remember(selectedRange, locale, database) {
        repository.getRecordsForRangeWithZeros(selectedRange)
    }
    val todayDeaths = remember(database) { repository.getTodayDeaths() }
    val totalDeaths = remember(database) { repository.getTotalDeaths() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Statistics",
                style = Typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Today's Deaths",
                            style = Typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$todayDeaths",
                            style = Typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 40.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Total Deaths",
                            style = Typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "$totalDeaths",
                            style = Typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 40.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Deaths Over Time",
                style = Typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TimeRange.entries.forEach { range ->
                    val selected = selectedRange == range
                    val shape by animateDpAsState(
                        targetValue = if (selected) 50.dp else 8.dp,
                        animationSpec = tween(durationMillis = 300),
                        label = "chipShape"
                    )
                    FilterChip(
                        selected = selected,
                        onClick = { selectedRange = range },
                        label = {
                            Box(contentAlignment = Alignment.Center) {
                            // Always bold — invisible, but drives the layout width
                            Text(
                                text = range.label,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.alpha(0f)
                            )
                            // Visible text — switches weight without affecting layout
                            Text(
                                text = range.label,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                        },
                        shape = RoundedCornerShape(shape),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            DeathsChart(
                records = records,
                locale = locale,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun DeathsChart(
    records: List<DeathRecord>,
    locale: Locale,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val density = LocalDensity.current
    val thresholdPx = with(density) { 20.dp.toPx() }

    val maxCount = (records.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)
    val minCount = 0

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (records.isEmpty() || records.all { it.count == 0 }) {
                Text(
                    text = "No data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurfaceVariantColor,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(records) {
                            detectTapGestures { offset ->
                                val width = size.width
                                val height = size.height
                                val padding = 40f
                                val chartWidth = width - padding * 2
                                val pointSpacing = if (records.size > 1) {
                                    chartWidth / (records.size - 1)
                                } else {
                                    0f
                                }

                                records.indices.forEach { index ->
                                    val x = padding + index * pointSpacing
                                    if (kotlin.math.abs(offset.x - x) < thresholdPx) {
                                        selectedIndex = index
                                        return@detectTapGestures
                                    }
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val padding = 40f
                    val chartWidth = width - padding * 2
                    val chartHeight = height - padding * 2

                    val pointSpacing = if (records.size > 1) {
                        chartWidth / (records.size - 1)
                    } else {
                        0f
                    }

                    drawLine(
                        color = surfaceVariantColor,
                        start = Offset(padding, height - padding),
                        end = Offset(width - padding, height - padding),
                        strokeWidth = 2f
                    )

                    drawLine(
                        color = surfaceVariantColor,
                        start = Offset(padding, padding),
                        end = Offset(padding, height - padding),
                        strokeWidth = 2f
                    )

                    val path = Path()
                    records.forEachIndexed { index, record ->
                        val x = padding + index * pointSpacing
                        val normalizedY = (record.count - minCount).toFloat() / (maxCount - minCount)
                        val y = height - padding - (normalizedY * chartHeight)

                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )

                    records.forEachIndexed { index, record ->
                        val x = padding + index * pointSpacing
                        val normalizedY = (record.count - minCount).toFloat() / (maxCount - minCount)
                        val y = height - padding - (normalizedY * chartHeight)

                        val isSelected = selectedIndex == index
                        val radius = if (isSelected) 12f else 8f

                        drawCircle(
                            color = primaryColor,
                            radius = radius,
                            center = Offset(x, y)
                        )

                        drawCircle(
                            color = Color.White,
                            radius = if (isSelected) 6f else 4f,
                            center = Offset(x, y)
                        )
                    }
                }

                selectedIndex?.let { index ->
                    if (index in records.indices) {
                        val record = records[index]
                        val formattedDate = formatDate(record.date, locale)
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$formattedDate: ${record.count} deaths",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(dateString: String, locale: Locale): String {
    return try {
        if (dateString.contains("W")) {
            dateString
        } else if (dateString.contains("-") && dateString.split("-").size == 2) {
            val yearMonth = YearMonth.parse(dateString)
            val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale)
            yearMonth.format(formatter)
        } else {
            val date = LocalDate.parse(dateString)
            val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", locale)
            date.format(formatter)
        }
    } catch (e: Exception) {
        Log.w("StatisticsScreen", "Failed to parse date: $dateString", e)
        dateString
    }
}
