package io.perfscope.sdk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.perfscope.sdk.data.MemorySpike
import io.perfscope.sdk.data.PerformanceMetrics
import io.perfscope.sdk.data.PerformanceViolation
import io.perfscope.sdk.data.ViolationSeverity

/**
 * Main performance overlay UI component.
 * Shows real-time performance metrics with budget violations.
 */
@Composable
fun PerfOverlay(
    metrics: PerformanceMetrics,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PerfScope",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    TextButton(onClick = onClose) {
                        Text("Close")
                    }
                }
                
                HorizontalDivider()
                
                // Performance Violations Section
                if (metrics.violations.isNotEmpty()) {
                    Text(
                        text = "⚠️ Performance Violations",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    metrics.violations.forEach { violation ->
                        ViolationCard(violation = violation)
                    }
                    
                    HorizontalDivider()
                }
                
                // Current Screen
                Text(
                    text = "Screen: ${metrics.currentScreen}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Memory Attribution Section
                metrics.memoryAttribution?.let { attribution ->
                    MemoryAttributionCard(attribution = attribution)
                }
                
                // Frame Attribution Section
                metrics.frameAttribution?.let { attribution ->
                    FrameAttributionCard(attribution = attribution)
                }
                
                // Standard Metrics
                MetricRow(
                    label = "Memory Usage",
                    value = "${metrics.memoryUsageMb} MB",
                    status = getMemoryStatus(metrics.memoryUsageMb)
                )
                
                MetricRow(
                    label = "Frame Rate",
                    value = "${metrics.frameRate} FPS",
                    status = getFrameRateStatus(metrics.frameRate)
                )
                
                MetricRow(
                    label = "App Size",
                    value = "${metrics.appSizeMb} MB",
                    status = MetricStatus.NORMAL
                )
                
                MetricRow(
                    label = "CPU Usage",
                    value = "${metrics.cpuUsagePercent}%",
                    status = getCpuStatus(metrics.cpuUsagePercent)
                )
            }
        }
    }
}

@Composable
private fun ViolationCard(
    violation: PerformanceViolation,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (violation.severity) {
                ViolationSeverity.CRITICAL -> MaterialTheme.colorScheme.errorContainer
                ViolationSeverity.HIGH -> Color(0xFFFFEBEE)
                ViolationSeverity.MEDIUM -> Color(0xFFFFF3E0)
                ViolationSeverity.LOW -> Color(0xFFF3E5F5)
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            when (violation.severity) {
                ViolationSeverity.CRITICAL -> MaterialTheme.colorScheme.error
                ViolationSeverity.HIGH -> Color.Red
                ViolationSeverity.MEDIUM -> Color(0xFFFF9800)
                ViolationSeverity.LOW -> Color(0xFF9C27B0)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = when (violation.severity) {
                            ViolationSeverity.CRITICAL -> MaterialTheme.colorScheme.error
                            ViolationSeverity.HIGH -> Color.Red
                            ViolationSeverity.MEDIUM -> Color(0xFFFF9800)
                            ViolationSeverity.LOW -> Color(0xFF9C27B0)
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Text(
                        text = "${violation.type.displayName} Violation",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Text(
                    text = violation.severity.displayName,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Actual: ${violation.actualValue}${getUnitForViolationType(violation.type)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Budget: ${violation.budgetValue}${getUnitForViolationType(violation.type)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "💡 ${violation.recommendation}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun MemoryAttributionCard(
    attribution: io.perfscope.sdk.data.MemoryAttribution,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (attribution.likelyCause) {
                MemorySpike.BITMAP_HEAVY, MemorySpike.NATIVE_HEAVY -> MaterialTheme.colorScheme.errorContainer
                MemorySpike.COLLECTION_HEAVY, MemorySpike.OBJECT_HEAVY -> MaterialTheme.colorScheme.warningContainer
                MemorySpike.NORMAL -> MaterialTheme.colorScheme.primaryContainer
                MemorySpike.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Memory Attribution",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${if (attribution.memoryDeltaMb > 0) "+" else ""}${attribution.memoryDeltaMb} MB",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (attribution.memoryDeltaMb > 0) Color.Red else Color.Green
                )
            }
            
            Text(
                text = "Cause: ${attribution.likelyCause.displayName}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = attribution.details,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    status: MetricStatus,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            StatusIndicator(status = status)
        }
    }
}

@Composable
private fun StatusIndicator(
    status: MetricStatus,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(8.dp)
            .background(
                color = when (status) {
                    MetricStatus.GOOD -> Color.Green
                    MetricStatus.WARNING -> Color.Yellow
                    MetricStatus.CRITICAL -> Color.Red
                    MetricStatus.NORMAL -> Color.Gray
                },
                shape = RoundedCornerShape(4.dp)
            )
    )
}

private fun getUnitForViolationType(type: io.perfscope.sdk.data.ViolationType): String {
    return when (type) {
        io.perfscope.sdk.data.ViolationType.HEAP_MEMORY,
        io.perfscope.sdk.data.ViolationType.SCREEN_MEMORY_DELTA,
        io.perfscope.sdk.data.ViolationType.BITMAP_SPIKE,
        io.perfscope.sdk.data.ViolationType.COLLECTION_SPIKE,
        io.perfscope.sdk.data.ViolationType.OBJECT_SPIKE,
        io.perfscope.sdk.data.ViolationType.NATIVE_SPIKE,
        io.perfscope.sdk.data.ViolationType.APP_SIZE -> " MB"
        io.perfscope.sdk.data.ViolationType.FRAME_RATE -> " FPS"
        io.perfscope.sdk.data.ViolationType.JANK_PERCENT -> "%"
        io.perfscope.sdk.data.ViolationType.FRAME_TIME,
        io.perfscope.sdk.data.ViolationType.SEVERE_JANK -> " ms"
        io.perfscope.sdk.data.ViolationType.CPU_USAGE -> "%"
    }
}

// Extension property for warning container color
private val ColorScheme.warningContainer: Color
    get() = Color(0xFFFFF3CD)

private fun getMemoryStatus(memoryMb: Int): MetricStatus {
    return when {
        memoryMb < 100 -> MetricStatus.GOOD
        memoryMb < 200 -> MetricStatus.WARNING
        else -> MetricStatus.CRITICAL
    }
}

private fun getFrameRateStatus(frameRate: Int): MetricStatus {
    return when {
        frameRate >= 55 -> MetricStatus.GOOD
        frameRate >= 45 -> MetricStatus.WARNING
        else -> MetricStatus.CRITICAL
    }
}

private fun getCpuStatus(cpuPercent: Int): MetricStatus {
    return when {
        cpuPercent < 30 -> MetricStatus.GOOD
        cpuPercent < 60 -> MetricStatus.WARNING
        else -> MetricStatus.CRITICAL
    }
}

enum class MetricStatus {
    GOOD, WARNING, CRITICAL, NORMAL
}

@Composable
private fun FrameAttributionCard(
    attribution: io.perfscope.sdk.data.FrameAttribution,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (attribution.jankType) {
                io.perfscope.sdk.data.JankType.MAIN_THREAD_BLOCKING -> MaterialTheme.colorScheme.errorContainer
                io.perfscope.sdk.data.JankType.LAYOUT_THRASH,
                io.perfscope.sdk.data.JankType.COMPOSE_RECOMPOSITION,
                io.perfscope.sdk.data.JankType.OVERDRAW -> MaterialTheme.colorScheme.warningContainer
                io.perfscope.sdk.data.JankType.SMOOTH -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Frame Attribution",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${String.format("%.1f", attribution.jankPercent)}% jank",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (attribution.jankPercent > 5f) Color.Red else Color.Green
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Type: ${attribution.jankType.displayName}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${String.format("%.1f", attribution.averageFrameMs)}ms avg",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = attribution.details,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}