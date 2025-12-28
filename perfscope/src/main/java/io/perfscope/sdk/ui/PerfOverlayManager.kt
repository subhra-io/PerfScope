package io.perfscope.sdk.ui

import android.content.Context
import androidx.compose.runtime.*
import io.perfscope.sdk.config.PerfScopeConfig
import io.perfscope.sdk.data.PerformanceMetrics
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
    
    init {
        // Update monitor with initial config
        performanceMonitor.updateBudgetConfig(initialConfig)
        
        // Start monitoring performance metrics
        performanceMonitor.startMonitoring { newMetrics ->
            _metrics.value = newMetrics
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
    
    @Composable
    fun ComposeOverlay() {
        if (_isVisible.value) {
            PerfOverlay(
                metrics = _metrics.value,
                onClose = { hide() }
            )
        }
    }
}