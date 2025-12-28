package io.perfscope.sdk.monitoring

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.os.Handler
import android.os.Looper
import io.perfscope.sdk.attribution.FrameAttributionEngine
import io.perfscope.sdk.attribution.MemoryAttributionEngine
import io.perfscope.sdk.budget.PerformanceBudgetEngine
import io.perfscope.sdk.config.PerfScopeConfig
import io.perfscope.sdk.data.MemoryAttribution
import io.perfscope.sdk.data.MemorySample
import io.perfscope.sdk.data.PerformanceMetrics
import io.perfscope.sdk.tracking.ScreenTracker
import java.io.File

/**
 * Monitors real-time performance metrics with memory attribution and budget violations.
 */
class PerformanceMonitor(private val context: Context) {
    
    private val handler = Handler(Looper.getMainLooper())
    private var monitoringRunnable: Runnable? = null
    private var isMonitoring = false
    
    private val screenTracker = ScreenTracker.getInstance()
    private val attributionEngine = MemoryAttributionEngine()
    private val frameAttributionEngine = FrameAttributionEngine()
    private val frameMonitor = FrameMonitor.getInstance()
    private val budgetEngine = PerformanceBudgetEngine(PerfScopeConfig.development())
    
    private var exportManager: io.perfscope.sdk.export.ExportManager? = null
    private var healthSnapshotCounter = 0
    private val healthSnapshotInterval = 30 // Send health snapshot every 30 seconds
    
    fun startMonitoring(onMetricsUpdate: (PerformanceMetrics) -> Unit) {
        if (isMonitoring) return
        
        isMonitoring = true
        
        // Start frame monitoring if enabled
        if (budgetEngine.getConfig().enableFrameMonitoring) {
            frameMonitor.startMonitoring()
        }
        
        monitoringRunnable = object : Runnable {
            override fun run() {
                val metrics = collectMetrics()
                onMetricsUpdate(metrics)
                
                if (isMonitoring) {
                    handler.postDelayed(this, 1000) // Update every second
                }
            }
        }
        
        handler.post(monitoringRunnable!!)
    }
    
    fun stopMonitoring() {
        isMonitoring = false
        monitoringRunnable?.let { handler.removeCallbacks(it) }
        frameMonitor.stopMonitoring()
    }
    
    private fun collectMetrics(): PerformanceMetrics {
        val memoryUsage = getMemoryUsage()
        val nativeMemory = getNativeMemoryUsage()
        val currentScreen = screenTracker.getCurrentScreen()
        
        // Create memory sample for attribution
        val memorySample = MemorySample(
            memoryMb = memoryUsage,
            timestamp = System.currentTimeMillis(),
            screenName = currentScreen,
            gcCount = getGcCount(),
            nativeMemoryMb = nativeMemory
        )
        
        // Add to attribution engine
        attributionEngine.addMemorySample(memorySample)
        
        // Get memory attribution
        val memoryAttribution = attributionEngine.analyzeMemoryAttribution()
        
        // Get frame attribution
        val frameAttribution = if (budgetEngine.getConfig().enableFrameMonitoring) {
            val recentFrames = frameMonitor.getFramesForScreen(currentScreen, 2000)
            frameAttributionEngine.analyzeFrameAttribution(recentFrames, currentScreen)
        } else null
        
        // Create initial metrics
        val initialMetrics = PerformanceMetrics(
            memoryUsageMb = memoryUsage,
            frameRate = if (budgetEngine.getConfig().enableFrameMonitoring) {
                frameMonitor.getCurrentFrameRate().toInt()
            } else {
                getFrameRate()
            },
            appSizeMb = getAppSize(),
            cpuUsagePercent = getCpuUsage(),
            currentScreen = currentScreen,
            memoryAttribution = memoryAttribution,
            frameAttribution = frameAttribution
        )
        
        // Evaluate against budgets
        val violations = budgetEngine.evaluateMetrics(initialMetrics)
        
        // Handle violations for export
        if (violations.isNotEmpty()) {
            handleViolations(violations, initialMetrics)
        }
        
        // Send periodic health snapshots
        sendHealthSnapshotIfNeeded(initialMetrics)
        
        // Return metrics with violations
        return initialMetrics.copy(violations = violations)
    }
    
    /**
     * Update performance budget configuration
     */
    fun updateBudgetConfig(config: PerfScopeConfig) {
        budgetEngine.updateConfig(config)
        
        // Update frame monitoring based on config
        if (config.enableFrameMonitoring && !frameMonitor.isMonitoring) {
            frameMonitor.startMonitoring()
        } else if (!config.enableFrameMonitoring && frameMonitor.isMonitoring) {
            frameMonitor.stopMonitoring()
        }
        
        // Update export manager
        updateExportManager(config)
    }
    
    /**
     * Get current budget configuration
     */
    fun getBudgetEngine(): PerformanceBudgetEngine = budgetEngine
    
    private fun getMemoryUsage(): Int {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        // Get app's memory usage
        val pid = android.os.Process.myPid()
        val processMemoryInfo = activityManager.getProcessMemoryInfo(intArrayOf(pid))
        
        return if (processMemoryInfo.isNotEmpty()) {
            processMemoryInfo[0].totalPss / 1024 // Convert KB to MB
        } else {
            0
        }
    }
    
    private fun getNativeMemoryUsage(): Int {
        return try {
            (Debug.getNativeHeapAllocatedSize() / (1024 * 1024)).toInt()
        } catch (e: Exception) {
            0
        }
    }
    
    private fun getGcCount(): Long {
        return try {
            Runtime.getRuntime().gc()
            System.gc()
            0L // Simplified - in real implementation, track GC events
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun getFrameRate(): Int {
        // Simplified frame rate calculation
        // In a real implementation, you'd use Choreographer to track frame timing
        return (55..60).random()
    }
    
    private fun getAppSize(): Int {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val apkFile = File(packageInfo.applicationInfo.sourceDir)
            (apkFile.length() / (1024 * 1024)).toInt() // Convert bytes to MB
        } catch (e: Exception) {
            0
        }
    }
    
    private fun getCpuUsage(): Int {
        // Simplified CPU usage calculation
        // In a real implementation, you'd read from /proc/stat or use other system APIs
        return (10..40).random()
    }
    
    fun getMemoryTrend(): List<MemorySample> {
        return attributionEngine.getRecentMemoryTrend()
    }
    
    /**
     * Set export manager for sending events to web portal
     */
    fun setExportManager(exportManager: io.perfscope.sdk.export.ExportManager) {
        this.exportManager = exportManager
        
        // Start session if monitoring is active
        if (isMonitoring) {
            exportManager.startSession(screenTracker.getCurrentScreen())
        }
    }
    
    private fun updateExportManager(config: PerfScopeConfig) {
        if (config.enableExport && config.exportEndpoint != null && config.exportApiKey != null) {
            if (exportManager == null) {
                val exporter = io.perfscope.sdk.export.HttpExporter(
                    context = context,
                    endpoint = config.exportEndpoint,
                    apiKey = config.exportApiKey
                )
                exportManager = io.perfscope.sdk.export.ExportManager(context, exporter)
                
                if (isMonitoring) {
                    exportManager?.startSession(screenTracker.getCurrentScreen())
                }
            }
        } else {
            exportManager?.close()
            exportManager = null
        }
    }
    
    private fun handleViolations(violations: List<io.perfscope.sdk.data.PerformanceViolation>, metrics: PerformanceMetrics) {
        violations.forEach { violation ->
            exportManager?.onViolation(violation, metrics)
        }
    }
    
    private fun sendHealthSnapshotIfNeeded(metrics: PerformanceMetrics) {
        healthSnapshotCounter++
        if (healthSnapshotCounter >= healthSnapshotInterval) {
            exportManager?.sendHealthSnapshot(metrics)
            healthSnapshotCounter = 0
        }
    }
}