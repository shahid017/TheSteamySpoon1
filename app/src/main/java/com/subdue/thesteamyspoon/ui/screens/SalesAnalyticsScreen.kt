package com.subdue.thesteamyspoon.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.subdue.thesteamyspoon.data.WeekdaySalesPoint
import com.subdue.thesteamyspoon.di.AppContainer
import com.subdue.thesteamyspoon.util.CurrencyFormatter
import com.subdue.thesteamyspoon.viewmodel.InvoiceViewModel
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesAnalyticsScreen(
    invoiceViewModel: InvoiceViewModel = viewModel(factory = AppContainer.viewModelFactory),
    onNavigateBack: () -> Unit
) {
    val period by invoiceViewModel.analyticsPeriod.collectAsState()
    val weekdaySales by invoiceViewModel.weekdaySales.collectAsState()
    val currencyFormat = CurrencyFormatter.getPKRFormatter()

    val best = remember(weekdaySales) { weekdaySales.maxByOrNull { it.totalSales } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales Analytics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Text("←", fontSize = 24.sp) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Cumulative Sales by Weekday",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            PeriodSelector(
                selected = period,
                onSelected = invoiceViewModel::setAnalyticsPeriod
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    WeekdayLineChart(
                        points = weekdaySales,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Requirement: show the cumulative sales value for each day (explicitly, per point).
                    WeekdayValueRow(
                        points = weekdaySales,
                        bestWeekday = best?.weekday,
                        valueFormatter = { currencyFormat.format(it) }
                    )
                }
            }

            best?.let {
                Text(
                    text = "Highest sales day: ${it.weekday} (${currencyFormat.format(it.totalSales)})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selected: InvoiceViewModel.AnalyticsPeriod,
    onSelected: (InvoiceViewModel.AnalyticsPeriod) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("Time period: ${selected.label}")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            InvoiceViewModel.AnalyticsPeriod.entries.forEach { p ->
                DropdownMenuItem(
                    text = { Text(p.label) },
                    onClick = {
                        onSelected(p)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun WeekdayLineChart(
    points: List<WeekdaySalesPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No sales data yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val yMax = max(1.0, points.maxOf { it.totalSales })
    val lineColor = MaterialTheme.colorScheme.primary
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    val bestColor = MaterialTheme.colorScheme.tertiary
    val bestWeekday = points.maxByOrNull { it.totalSales }?.weekday

    Canvas(modifier = modifier) {
        val paddingLeft = 24f
        val paddingRight = 16f
        val paddingTop = 16f
        val paddingBottom = 24f

        val chartW = (size.width - paddingLeft - paddingRight).coerceAtLeast(1f)
        val chartH = (size.height - paddingTop - paddingBottom).coerceAtLeast(1f)

        // Axes
        drawLine(
            color = axisColor,
            start = Offset(paddingLeft, paddingTop),
            end = Offset(paddingLeft, paddingTop + chartH),
            strokeWidth = 2f
        )
        drawLine(
            color = axisColor,
            start = Offset(paddingLeft, paddingTop + chartH),
            end = Offset(paddingLeft + chartW, paddingTop + chartH),
            strokeWidth = 2f
        )

        fun xFor(i: Int): Float {
            if (points.size == 1) return paddingLeft + chartW / 2f
            return paddingLeft + (i.toFloat() / (points.size - 1).toFloat()) * chartW
        }

        fun yFor(value: Double): Float {
            val t = (value / yMax).toFloat().coerceIn(0f, 1f)
            return paddingTop + (1f - t) * chartH
        }

        val path = Path()
        points.forEachIndexed { index, p ->
            val x = xFor(index)
            val y = yFor(p.totalSales)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )

        points.forEachIndexed { index, p ->
            val x = xFor(index)
            val y = yFor(p.totalSales)
            val isBest = p.weekday == bestWeekday
            drawCircle(
                color = if (isBest) bestColor else lineColor,
                radius = if (isBest) 7f else 4.5f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun WeekdayValueRow(
    points: List<WeekdaySalesPoint>,
    bestWeekday: String?,
    valueFormatter: (Double) -> String
) {
    val abbreviations = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val ordered = if (points.size == 7) points else points.sortedBy { it.weekday }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ordered.forEachIndexed { index, p ->
            val isBest = p.weekday == bestWeekday
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = abbreviations.getOrNull(index) ?: p.weekday.take(3),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isBest) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = valueFormatter(p.totalSales),
                    fontSize = 11.sp,
                    fontWeight = if (isBest) FontWeight.Bold else FontWeight.Normal,
                    color = if (isBest) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


