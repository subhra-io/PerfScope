package io.perfscope.sdk.ui

import android.content.Context
import androidx.compose.runtime.*
import io.perfscope.sdk.config.PerfScopeConfig
import io.perfscope.sdk.data.PerformanceMetrics
import io.perfscope.sdk.export.ExportManager
import io.perfscope.sdk.export.HttpExporter
import io.perfscope.sdk.monitoring.PerformanceMonitor

/**
 * Manages the lifecycle and state of the performance overlay UI with budget configuration.
 */
class PerfOverlayManager(private val context: Context, initialConfig: PerfScopeConfig = PerfScopeConfig.development()) {
    
    private val performanceMonitor = PerformanceMonitor(context)
    private var _isVisible = mutableStateOf(false)
    private var _metrics = mutableStateOf(PerformanceMetrics())
    
    val isVisible: State<Boolean> = _isVisible
    val metrics: State<PerformanceMetrics> = _metrics
    
    // Export manager for sending data to backend
    private val exportManager: ExportManager? = if (initialConfig.enableExport) {
        try {
            val exporter = HttpExporter(context, initialConfig)
            ExportManager(context, exporter).also { manager ->
                manager.startSession("MainActivity") // Default initial screen
            }
        } catch (e: Exception) {
            if (initialConfig.enableDebugLogs) {
                android.util.Log.w("PerfScope", "Failed to initialize export manager", e)
            }
            null
        }
    } else null
    
    init {
        // Update monitor with initial config
        performanceMonitor.updateBudgetConfig(initialConfig)
        
        // Start monitoring performance metrics
        performanceMonitor.startMonitoring { newMetrics ->
            _metrics.value = newMetrics
            
            // Send health snapshots to backend if export is enabled
            exportManager?.sendHealthSnapshot(newMetrics)
            
            // Send violations to backend if any
            newMetrics.violations.forEach { violation ->
                exportManager?.onViolation(violation, newMetrics)
            }
        }
    }
    
    fun show() {
        _isVisible.value = true
    }
    
    fun hide() {
        _isVisible.value = false
    }
    
    fun isVisible(): Boolean {
        return _isVisible.value
    }
    
    fun updateBudgetConfig(config: PerfScopeConfig) {
        performanceMonitor.updateBudgetConfig(config)
    }
    
    fun onScreenChange(screenName: String) {
        exportManager?.onScreenChange(screenName)
    }
    
    @Composable
    fun ComposeOverlay() {
        if (_isVisible.value) {
            PerfOverlay(
                metrics = _metrics.value,
                onClose = { hide() }
            )
        }
    }
    
    fun close() {
        exportManager?.close()
    }