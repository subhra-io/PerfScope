package io.perfscope.sdk.attribution

import io.perfscope.sdk.data.MemoryAttribution
import io.perfscope.sdk.data.MemorySample
import io.perfscope.sdk.data.MemorySpike
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.abs

/**
 * Analyzes memory samples to provide attribution insights
 */
class MemoryAttributionEngine {
    
    private val memorySamples = ConcurrentLinkedQueue<MemorySample>()
    private val maxSamples = 50 // Keep last 50 samples
    private val minDeltaForAttribution = 5 // MB threshold for attribution
    
    fun addMemorySample(sample: MemorySample) {
        memorySamples.offer(sample)
        
        // Keep only last N samples
        while (memorySamples.size > maxSamples) {
            memorySamples.poll()
        }
    }
    
    fun analyzeMemoryAttribution(): MemoryAttribution? {
        val samples = memorySamples.toList()
        if (samples.size < 2) return null
        
        val currentSample = samples.last()
        val previousScreenSamples = findPreviousScreenSamples(samples, currentSample.screenName)
        
        if (previousScreenSamples.isEmpty()) return null
        
        val baselineMemory = previousScreenSamples.first().memoryMb
        val currentMemory = currentSample.memoryMb
        val memoryDelta = currentMemory - baselineMemory
        
        // Only create attribution for significant changes
        if (abs(memoryDelta) < minDeltaForAttribution) return null
        
        val likelyCause = classifyMemorySpike(samples, currentSample, memoryDelta)
        
        return MemoryAttribution(
            screenName = currentSample.screenName,
            memoryDeltaMb = memoryDelta,
            likelyCause = likelyCause,
            details = generateDetails(likelyCause, memoryDelta, currentSample)
        )
    }
    
    private fun findPreviousScreenSamples(samples: List<MemorySample>, screenName: String): List<MemorySample> {
        val screenSamples = samples.filter { it.screenName == screenName }
        return if (screenSamples.size >= 2) {
            screenSamples.take(screenSamples.size - 1) // All but the last one
        } else {
            // If no previous samples for this screen, use the last sample from previous screen
            val lastDifferentScreen = samples.findLast { it.screenName != screenName }
            if (lastDifferentScreen != null) listOf(lastDifferentScreen) else emptyList()
        }
    }
    
    private fun classifyMemorySpike(
        samples: List<MemorySample>, 
        currentSample: MemorySample, 
        memoryDelta: Int
    ): MemorySpike {
        val recentSamples = samples.takeLast(5)
        
        // Analyze memory growth pattern
        val growthRate = calculateGrowthRate(recentSamples)
        val isRapidGrowth = growthRate > 10 // MB per sample
        val isSteadyGrowth = growthRate > 2 && growthRate <= 10
        
        return when {
            // Large sudden spike - likely bitmaps/images
            memoryDelta > 20 && isRapidGrowth -> MemorySpike.BITMAP_HEAVY
            
            // Steady growth - likely collections
            memoryDelta > 10 && isSteadyGrowth -> MemorySpike.COLLECTION_HEAVY
            
            // Native memory indicators (simplified heuristic)
            currentSample.nativeMemoryMb > 0 && memoryDelta > 15 -> MemorySpike.NATIVE_HEAVY
            
            // Many small allocations
            memoryDelta > 5 && memoryDelta <= 15 -> MemorySpike.OBJECT_HEAVY
            
            // Normal expected growth
            memoryDelta > 0 && memoryDelta <= 5 -> MemorySpike.NORMAL
            
            else -> MemorySpike.UNKNOWN
        }
    }
    
    private fun calculateGrowthRate(samples: List<MemorySample>): Double {
        if (samples.size < 2) return 0.0
        
        val first = samples.first().memoryMb
        val last = samples.last().memoryMb
        val sampleCount = samples.size
        
        return (last - first).toDouble() / sampleCount
    }
    
    private fun generateDetails(spike: MemorySpike, delta: Int, sample: MemorySample): String {
        return when (spike) {
            MemorySpike.BITMAP_HEAVY -> "Check image loading, bitmap caching, or large graphics"
            MemorySpike.COLLECTION_HEAVY -> "Review list/array growth, data structures, or caching"
            MemorySpike.NATIVE_HEAVY -> "Investigate native libraries, JNI calls, or external resources"
            MemorySpike.OBJECT_HEAVY -> "Many objects created - check loops, factories, or builders"
            MemorySpike.NORMAL -> "Expected memory usage for this screen"
            MemorySpike.UNKNOWN -> "Memory increase detected - investigate recent code changes"
        }
    }
    
    fun getRecentMemoryTrend(): List<MemorySample> {
        return memorySamples.toList().takeLast(10)
    }
}