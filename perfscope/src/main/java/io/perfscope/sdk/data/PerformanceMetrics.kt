package io.perfscope.sdk.data

/**
 * Data class representing current performance metrics with memory attribution, frame attribution, and violations.
 */
data class PerformanceMetrics(
    val memoryUsageMb: Int = 0,
    val frameRate: Int = 60,
    val appSizeMb: Int = 0,
    val cpuUsagePercent: Int = 0,
    val currentScreen: String = "Unknown",
    val memoryAttribution: MemoryAttribution? = null,
    val frameAttribution: FrameAttribution? = null,
    val violations: List<PerformanceViolation> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)