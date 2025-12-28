package io.perfscope.sdk.config

/**
 * Configuration for performance budgets and thresholds.
 * Defines what is acceptable vs. what constitutes a violation.
 */
data class PerfScopeConfig(
    // Memory Budgets
    val maxHeapMb: Int = 200,
    val maxScreenDeltaMb: Int = 30,
    val maxBitmapSpikeMb: Int = 25,
    val maxCollectionSpikeMb: Int = 20,
    val maxObjectSpikeMb: Int = 15,
    val maxNativeSpikeMb: Int = 30,
    
    // Frame/UX Budgets
    val maxJankPercent: Float = 5f,        // Maximum acceptable jank percentage
    val maxFrameMs: Float = 24f,           // Maximum acceptable frame time (24ms = ~42fps)
    val maxSevereJankMs: Float = 50f,      // Threshold for severe jank classification
    
    // Performance Budgets
    val minFrameRate: Int = 50,
    val maxCpuPercent: Int = 70,
    val maxAppSizeMb: Int = 100,
    
    // Violation Settings
    val enableViolationAlerts: Boolean = true,
    val violationCooldownMs: Long = 5000, // Don't spam violations
    val trackViolationHistory: Boolean = true,
    
    // Frame Monitoring Settings
    val enableFrameMonitoring: Boolean = true,
    val frameAnalysisWindowMs: Long = 2000, // 2 second analysis window
    
    // Export Settings
    val enableExport: Boolean = false,
    val exportEndpoint: String? = null,
    val exportApiKey: String? = null
) {
    companion object {
        /**
         * Default configuration for development builds
         */
        fun development() = PerfScopeConfig(
            maxHeapMb = 150,
            maxScreenDeltaMb = 20,
            maxBitmapSpikeMb = 15,
            maxJankPercent = 3f,           // Strict jank budget for dev
            maxFrameMs = 20f,              // Strict frame time budget
            enableViolationAlerts = true,
            enableFrameMonitoring = true,
            enableExport = false           // Disabled by default in dev
        )
        
        /**
         * Configuration with export enabled for web portal
         */
        fun withExport(endpoint: String, apiKey: String) = development().copy(
            enableExport = true,
            exportEndpoint = endpoint,
            exportApiKey = apiKey
        )
        
        /**
         * Stricter configuration for production monitoring
         */
        fun production() = PerfScopeConfig(
            maxHeapMb = 120,
            maxScreenDeltaMb = 15,
            maxBitmapSpikeMb = 10,
            maxCollectionSpikeMb = 12,
            maxJankPercent = 8f,           // More lenient for production
            maxFrameMs = 30f,              // More lenient frame time
            enableViolationAlerts = false, // Don't show UI in production
            trackViolationHistory = true,
            enableFrameMonitoring = true,
            enableExport = true            // Enable export in production
        )
        
        /**
         * Relaxed configuration for testing
         */
        fun testing() = PerfScopeConfig(
            maxHeapMb = 300,
            maxScreenDeltaMb = 50,
            maxJankPercent = 15f,          // Very relaxed for testing
            maxFrameMs = 50f,
            enableViolationAlerts = false,
            enableFrameMonitoring = false, // Disable frame monitoring in tests
            enableExport = false           // Disable export in tests
        )
        
        /**
         * Ultra-strict configuration for performance-critical apps
         */
        fun performanceCritical() = PerfScopeConfig(
            maxHeapMb = 100,
            maxScreenDeltaMb = 10,
            maxBitmapSpikeMb = 8,
            maxJankPercent = 1f,           // Ultra-strict jank budget
            maxFrameMs = 16.67f,           // 60fps strict
            maxSevereJankMs = 25f,         // Lower severe jank threshold
            enableViolationAlerts = true,
            enableFrameMonitoring = true,
            enableExport = true            // Export for analysis
        )
    }
}