package io.perfscope.sdk.attribution

import io.perfscope.sdk.data.FrameAttribution
import io.perfscope.sdk.data.FrameSample
import io.perfscope.sdk.data.FrameWindow
import io.perfscope.sdk.data.JankType
import kotlin.math.max

/**
 * Analyzes frame samples to provide jank attribution insights
 */
class FrameAttributionEngine {
    
    private val analysisWindowMs = 2000L // 2 second analysis window
    private val minFramesForAnalysis = 30 // Minimum frames needed for reliable analysis
    
    fun analyzeFrameAttribution(frames: List<FrameSample>, screenName: String): FrameAttribution? {
        if (frames.size < minFramesForAnalysis) return null
        
        val window = FrameWindow(
            samples = frames,
            windowStartMs = frames.firstOrNull()?.timestamp ?: 0L,
            windowDurationMs = analysisWindowMs,
            screenName = screenName
        )
        
        val jankType = classifyJankType(window)
        
        return FrameAttribution(
            screenName = screenName,
            jankPercent = window.jankPercent,
            averageFrameMs = window.averageFrameMs,
            jankType = jankType,
            frameCount = window.frameCount,
            jankFrameCount = window.jankFrameCount,
            details = generateJankDetails(jankType, window)
        )
    }
    
    private fun classifyJankType(window: FrameWindow): JankType {
        val jankFrames = window.samples.filter { it.isJank }
        
        if (jankFrames.isEmpty()) {
            return JankType.SMOOTH
        }
        
        val averageJankTime = jankFrames.map { it.frameTimeMs }.average()
        val maxFrameTime = jankFrames.maxOfOrNull { it.frameTimeMs } ?: 0f
        val jankPercent = window.jankPercent
        
        // Analyze jank patterns to classify type
        return when {
            // Severe blocking (>100ms frames) - likely main thread blocking
            maxFrameTime > 100f -> JankType.MAIN_THREAD_BLOCKING
            
            // Many moderate janks (consistent 30-50ms) - likely layout thrashing
            jankPercent > 15f && averageJankTime in 30f..50f -> JankType.LAYOUT_THRASH
            
            // Frequent mild janks (20-35ms) - likely Compose recomposition
            jankPercent > 10f && averageJankTime in 20f..35f -> JankType.COMPOSE_RECOMPOSITION
            
            // Consistent moderate janks - could be overdraw
            jankPercent > 8f && averageJankTime in 25f..40f -> JankType.OVERDRAW
            
            // Sporadic severe janks - likely memory pressure/GC
            jankPercent < 8f && maxFrameTime > 50f -> JankType.MEMORY_PRESSURE
            
            // High average frame time with moderate jank - GPU bottleneck
            window.averageFrameMs > 20f && jankPercent > 5f -> JankType.GPU_BOTTLENECK
            
            else -> JankType.UNKNOWN
        }
    }
    
    private fun generateJankDetails(jankType: JankType, window: FrameWindow): String {
        val jankPercent = "%.1f".format(window.jankPercent)
        val avgFrame = "%.1f".format(window.averageFrameMs)
        
        return when (jankType) {
            JankType.MAIN_THREAD_BLOCKING -> 
                "Heavy main thread work detected. Move long operations to background threads."
            
            JankType.LAYOUT_THRASH -> 
                "Excessive layout calculations. Check for nested layouts or frequent layout changes."
            
            JankType.COMPOSE_RECOMPOSITION -> 
                "Heavy Compose recompositions. Optimize composables and reduce unnecessary recompositions."
            
            JankType.OVERDRAW -> 
                "Overdraw detected. Reduce overlapping views and optimize drawing operations."
            
            JankType.MEMORY_PRESSURE -> 
                "Memory pressure causing GC pauses. Optimize memory usage and object allocations."
            
            JankType.GPU_BOTTLENECK -> 
                "GPU processing bottleneck. Reduce graphics complexity or shader operations."
            
            JankType.SMOOTH -> 
                "Smooth performance maintained. $jankPercent% jank, ${avgFrame}ms avg frame time."
            
            JankType.UNKNOWN -> 
                "Jank detected but cause unclear. $jankPercent% jank, investigate recent changes."
        }
    }
    
    /**
     * Analyze frame performance trends over time
     */
    fun analyzeFrameTrends(frames: List<FrameSample>): FrameTrendAnalysis {
        if (frames.isEmpty()) {
            return FrameTrendAnalysis()
        }
        
        val sortedFrames = frames.sortedBy { it.timestamp }
        val windowSize = max(30, frames.size / 4) // Quarter of frames or minimum 30
        
        val windows = sortedFrames.chunked(windowSize).map { chunk ->
            FrameWindow(
                samples = chunk,
                windowStartMs = chunk.first().timestamp,
                windowDurationMs = chunk.last().timestamp - chunk.first().timestamp,
                screenName = chunk.first().screenName
            )
        }
        
        val isImproving = if (windows.size >= 2) {
            windows.last().jankPercent < windows.first().jankPercent
        } else false
        
        val isDegrading = if (windows.size >= 2) {
            windows.last().jankPercent > windows.first().jankPercent + 2f // 2% threshold
        } else false
        
        return FrameTrendAnalysis(
            windows = windows,
            isImproving = isImproving,
            isDegrading = isDegrading,
            overallJankPercent = frames.count { it.isJank }.toFloat() / frames.size * 100f
        )
    }
}

/**
 * Frame performance trend analysis
 */
data class FrameTrendAnalysis(
    val windows: List<FrameWindow> = emptyList(),
    val isImproving: Boolean = false,
    val isDegrading: Boolean = false,
    val overallJankPercent: Float = 0f
)