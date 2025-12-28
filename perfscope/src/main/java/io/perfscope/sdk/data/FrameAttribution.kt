package io.perfscope.sdk.data

/**
 * Represents frame performance attribution for a specific screen/context
 */
data class FrameAttribution(
    val screenName: String,
    val jankPercent: Float,
    val averageFrameMs: Float,
    val jankType: JankType,
    val frameCount: Int,
    val jankFrameCount: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String = ""
)

/**
 * Classification of jank types by cause
 */
enum class JankType(val displayName: String, val description: String) {
    MAIN_THREAD_BLOCKING("Main thread blocking", "Long-running operations blocking UI thread"),
    LAYOUT_THRASH("Layout thrashing", "Excessive layout calculations or nested layouts"),
    COMPOSE_RECOMPOSITION("Heavy recomposition", "Excessive Compose recompositions or heavy composables"),
    OVERDRAW("Overdraw issues", "Too many overlapping draw operations"),
    MEMORY_PRESSURE("Memory pressure", "GC pauses or memory allocation causing frame drops"),
    GPU_BOTTLENECK("GPU bottleneck", "Graphics processing taking too long"),
    UNKNOWN("Unknown cause", "Frame drops detected but cause unclear"),
    SMOOTH("Smooth performance", "No significant jank detected")
}

/**
 * Frame timing sample
 */
data class FrameSample(
    val frameTimeMs: Float,
    val timestamp: Long,
    val screenName: String,
    val isJank: Boolean = frameTimeMs > 16.67f, // 60fps threshold
    val frameNumber: Long = 0
)

/**
 * Frame performance window for analysis
 */
data class FrameWindow(
    val samples: List<FrameSample>,
    val windowStartMs: Long,
    val windowDurationMs: Long,
    val screenName: String
) {
    val averageFrameMs: Float
        get() = if (samples.isNotEmpty()) samples.map { it.frameTimeMs }.average().toFloat() else 0f
    
    val jankPercent: Float
        get() = if (samples.isNotEmpty()) (samples.count { it.isJank }.toFloat() / samples.size) * 100f else 0f
    
    val frameCount: Int
        get() = samples.size
    
    val jankFrameCount: Int
        get() = samples.count { it.isJank }
}