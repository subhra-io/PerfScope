package io.perfscope.sdk

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import io.perfscope.sdk.config.PerfScopeConfig
import io.perfscope.sdk.tracking.ScreenTracker
import io.perfscope.sdk.ui.PerfOverlayManager

/**
 * Main entry point for the PerfScope SDK.
 * 
 * This SDK provides real-time visibility into app performance, memory usage,
 * and app size directly inside the running app with performance budget enforcement.
 */
object PerfScope {
    
    private var isInitialized = false
    private lateinit var overlayManager: PerfOverlayManager
    private lateinit var screenTracker: ScreenTracker
    
    /**
     * Initialize the PerfScope SDK with default development configuration.
     * Call this once in your Application.onCreate() or MainActivity.onCreate()
     * 
     * @param context Application or Activity context
     */
    fun init(context: Context) {
        init(context, PerfScopeConfig.development())
    }
    
    /**
     * Initialize the PerfScope SDK with custom configuration.
     * 
     * @param context Application or Activity context
     * @param config Performance budget configuration
     */
    fun init(context: Context, config: PerfScopeConfig) {
        if (isInitialized) return
        
        val appContext = context.applicationContext
        overlayManager = PerfOverlayManager(appContext, config)
        
        // Initialize screen tracking
        screenTracker = ScreenTracker.getInstance()
        if (appContext is Application) {
            screenTracker.initialize(appContext)
        }
        
        isInitialized = true
    }
    
    /**
     * Show the performance overlay UI
     */
    fun showOverlay() {
        checkInitialized()
        overlayManager.show()
    }
    
    /**
     * Hide the performance overlay UI
     */
    fun hideOverlay() {
        checkInitialized()
        overlayManager.hide()
    }
    
    /**
     * Check if the overlay is currently visible
     */
    fun isOverlayVisible(): Boolean {
        checkInitialized()
        return overlayManager.isVisible()
    }
    
    /**
     * Update performance budget configuration
     */
    fun updateBudgetConfig(config: PerfScopeConfig) {
        checkInitialized()
        overlayManager.updateBudgetConfig(config)
    }
    
    /**
     * Manually set the current screen name for attribution
     * Useful for Compose screens or custom screen naming
     */
    fun setCurrentScreen(screenName: String) {
        if (isInitialized) {
            screenTracker.setCurrentScreen(screenName)
        }
    }
    
    /**
     * Compose function to render the overlay when visible.
     * Call this in your main Compose content.
     */
    @Composable
    fun OverlayContent() {
        if (isInitialized) {
            overlayManager.ComposeOverlay()
        }
    }
    
    private fun checkInitialized() {
        if (!isInitialized) {
            throw IllegalStateException("PerfScope must be initialized first. Call PerfScope.init(context)")
        }
    }
}