package io.perfscope.sdk.export

import android.content.Context
import android.os.Handler
import android.os.Looper
import io.perfscope.sdk.data.FrameAttribution
import io.perfscope.sdk.data.MemoryAttribution
import io.perfscope.sdk.data.PerformanceMetrics
import io.perfscope.sdk.data.PerformanceViolation
import io.perfscope.sdk.data.ViolationType

/**
 * Manages the export of performance events to external systems.
 * Coordinates between the SDK's internal data and the export layer.
 */
class ExportManager(
    context: Context,
    private val exporter: PerfScopeExporter
) {
    
    private val eventFactory = EventFactory(context)
    private val handler = Handler(Looper.getMainLooper())
    
    private var sessionStartTime = System.currentTimeMillis()
    private var currentScreen = "Unknown"
    private var previousScreen = "Unknown"
    private var screenStartTime = System.currentTimeMillis()
    private var totalViolations = 0
    
    private var healthSnapshotRunnable: Runnable? = null
    private val healthSnapshotIntervalMs = 30000L // 30 seconds
    
    fun startSession(initialScreen: String) {
        sessionStartTime = System.currentTimeMillis()
        currentScreen = initialScreen
        screenStartTime = System.currentTimeMillis()
        totalViolations = 0
        
        // Send session start event
        val event = eventFactory.createSessionStartEvent(initialScreen)
        exporter.send(event)
        
        // Start periodic health snapshots
        startHealthSnapshots()
    }
    
    fun endSession() {
        stopHealthSnapshots()
        
        val durationMs = System.currentTimeMillis() - sessionStartTime
        val event = eventFactory.createSessionEndEvent(
            screen = currentScreen,
            durationMs = durationMs,
            totalViolations = totalViolations
        )
        exporter.send(event)
        exporter.flush()
    }
    
    fun onScreenChange(newScreen: String) {
        val now = System.currentTimeMillis()
        val timeOnPreviousScreen = now - screenStartTime
        
        // Send screen change event
        val event = eventFactory.createScreenChangeEvent(
            currentScreen = newScreen,
            previousScreen = currentScreen,
            timeOnPreviousScreenMs = timeOnPreviousScreen
        )
        exporter.send(event)
        
        // Update state
        previousScreen = currentScreen
        currentScreen = newScreen
        screenStartTime = now
    }
    
    fun onViolation(violation: PerformanceViolation, metrics: PerformanceMetrics) {
        totalViolations++
        
        when {
            isMemoryViolation(violation.type) && metrics.memoryAttribution != null -> {
                val event = eventFactory.createMemoryViolationEvent(
                    screen = currentScreen,
                    violation = violation,
                    attribution = metrics.memoryAttribution
                )
                exporter.send(event)
            }
            
            isJankViolation(violation.type) && metrics.frameAttribution != null -> {
                val event = eventFactory.createJankViolationEvent(
                    screen = currentScreen,
                    violation = violation,
                    attribution = metrics.frameAttribution
                )
                exporter.send(event)
            }
        }
    }
    
    private fun startHealthSnapshots() {
        healthSnapshotRunnable = object : Runnable {
            override fun run() {
                // This would be called with current metrics
                // For now, we'll trigger it from the performance monitor
                handler.postDelayed(this, healthSnapshotIntervalMs)
            }
        }
        handler.post(healthSnapshotRunnable!!)
    }
    
    private fun stopHealthSnapshots() {
        healthSnapshotRunnable?.let { handler.removeCallbacks(it) }
        healthSnapshotRunnable = null
    }
    
    fun sendHealthSnapshot(metrics: PerformanceMetrics) {
        val jankPercent = metrics.frameAttribution?.jankPercent ?: 0f
        val avgFrameMs = metrics.frameAttribution?.averageFrameMs ?: 16.67f
        
        val event = eventFactory.createHealthSnapshotEvent(
            screen = currentScreen,
            memoryMb = metrics.memoryUsageMb,
            jankPercent = jankPercent,
            avgFrameMs = avgFrameMs,
            cpuPercent = metrics.cpuUsagePercent
        )
        exporter.send(event)
    }
    
    private fun isMemoryViolation(type: ViolationType): Boolean {
        return when (type) {
            ViolationType.HEAP_MEMORY,
            ViolationType.SCREEN_MEMORY_DELTA,
            ViolationType.BITMAP_SPIKE,
            ViolationType.COLLECTION_SPIKE,
            ViolationType.OBJECT_SPIKE,
            ViolationType.NATIVE_SPIKE -> true
            else -> false
        }
    }
    
    private fun isJankViolation(type: ViolationType): Boolean {
        return when (type) {
            ViolationType.JANK_PERCENT,
            ViolationType.FRAME_TIME,
            ViolationType.SEVERE_JANK -> true
            else -> false
        }
    }
    
    fun getSessionId(): String = eventFactory.getSessionId()
    
    fun flush() {
        exporter.flush()
    }
    
    fun close() {
        endSession()
        exporter.close()
    }
}