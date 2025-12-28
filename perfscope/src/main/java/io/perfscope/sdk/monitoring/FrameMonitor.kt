package io.perfscope.sdk.monitoring

import android.view.Choreographer
import io.perfscope.sdk.data.FrameSample
import io.perfscope.sdk.tracking.ScreenTracker
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Monitors frame timing using Choreographer for accurate jank detection
 */
class FrameMonitor private constructor() : Choreographer.FrameCallback {
    
    private val choreographer = Choreographer.getInstance()
    private val screenTracker = ScreenTracker.getInstance()
    private val frameSamples = ConcurrentLinkedQueue<FrameSample>()
    private val maxSamples = 300 // Keep last 5 seconds at 60fps
    
    private var _isMonitoring = false
    private var lastFrameTimeNs = 0L
    private var frameNumber = 0L
    private val frameListeners = mutableListOf<(FrameSample) -> Unit>()
    
    val isMonitoring: Boolean get() = _isMonitoring
    
    companion object {
        @Volatile
        private var INSTANCE: FrameMonitor? = null
        
        fun getInstance(): FrameMonitor {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FrameMonitor().also { INSTANCE = it }
            }
        }
    }
    
    fun startMonitoring() {
        if (_isMonitoring) return
        
        _isMonitoring = true
        lastFrameTimeNs = System.nanoTime()
        choreographer.postFrameCallback(this)
    }
    
    fun stopMonitoring() {
        _isMonitoring = false
        choreographer.removeFrameCallback(this)
    }
    
    override fun doFrame(frameTimeNs: Long) {
        if (!_isMonitoring) return
        
        if (lastFrameTimeNs != 0L) {
            val frameTimeMs = (frameTimeNs - lastFrameTimeNs) / 1_000_000f
            val currentScreen = screenTracker.getCurrentScreen()
            
            val frameSample = FrameSample(
                frameTimeMs = frameTimeMs,
                timestamp = System.currentTimeMillis(),
                screenName = currentScreen,
                frameNumber = frameNumber++
            )
            
            addFrameSample(frameSample)
            notifyFrameListeners(frameSample)
        }
        
        lastFrameTimeNs = frameTimeNs
        
        // Schedule next frame callback
        if (_isMonitoring) {
            choreographer.postFrameCallback(this)
        }
    }
    
    private fun addFrameSample(sample: FrameSample) {
        frameSamples.offer(sample)
        
        // Keep only recent samples
        while (frameSamples.size > maxSamples) {
            frameSamples.poll()
        }
    }
    
    private fun notifyFrameListeners(sample: FrameSample) {
        frameListeners.forEach { it(sample) }
    }
    
    fun addFrameListener(listener: (FrameSample) -> Unit) {
        frameListeners.add(listener)
    }
    
    fun removeFrameListener(listener: (FrameSample) -> Unit) {
        frameListeners.remove(listener)
    }
    
    /**
     * Get recent frame samples for analysis
     */
    fun getRecentFrames(durationMs: Long = 1000): List<FrameSample> {
        val cutoffTime = System.currentTimeMillis() - durationMs
        return frameSamples.filter { it.timestamp >= cutoffTime }.toList()
    }
    
    /**
     * Get frame samples for a specific screen
     */
    fun getFramesForScreen(screenName: String, durationMs: Long = 2000): List<FrameSample> {
        val cutoffTime = System.currentTimeMillis() - durationMs
        return frameSamples.filter { 
            it.screenName == screenName && it.timestamp >= cutoffTime 
        }.toList()
    }
    
    /**
     * Get current frame rate (frames per second)
     */
    fun getCurrentFrameRate(): Float {
        val recentFrames = getRecentFrames(1000) // Last second
        return if (recentFrames.isNotEmpty()) {
            recentFrames.size.toFloat()
        } else {
            0f
        }
    }
    
    /**
     * Get current jank percentage
     */
    fun getCurrentJankPercent(): Float {
        val recentFrames = getRecentFrames(1000)
        return if (recentFrames.isNotEmpty()) {
            (recentFrames.count { it.isJank }.toFloat() / recentFrames.size) * 100f
        } else {
            0f
        }
    }
    
    /**
     * Get average frame time in milliseconds
     */
    fun getAverageFrameTime(): Float {
        val recentFrames = getRecentFrames(1000)
        return if (recentFrames.isNotEmpty()) {
            recentFrames.map { it.frameTimeMs }.average().toFloat()
        } else {
            16.67f // 60fps baseline
        }
    }
}