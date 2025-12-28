package io.perfscope.sdk.budget

import io.perfscope.sdk.config.PerfScopeConfig
import io.perfscope.sdk.data.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Evaluates performance metrics against configured budgets and identifies violations
 */
class PerformanceBudgetEngine(private var config: PerfScopeConfig) {
    
    private val violationHistory = mutableListOf<ViolationHistoryEntry>()
    private val lastViolationTime = ConcurrentHashMap<ViolationType, Long>()
    private val violationListeners = mutableListOf<(PerformanceViolation) -> Unit>()
    
    /**
     * Evaluate current performance metrics against budgets
     */
    fun evaluateMetrics(metrics: PerformanceMetrics): List<PerformanceViolation> {
        val violations = mutableListOf<PerformanceViolation>()
        
        // Check heap memory budget
        if (metrics.memoryUsageMb > config.maxHeapMb) {
            violations.add(createViolation(
                type = ViolationType.HEAP_MEMORY,
                screenName = metrics.currentScreen,
                actualValue = metrics.memoryUsageMb,
                budgetValue = config.maxHeapMb,
                recommendation = "Reduce memory usage by optimizing object lifecycle, clearing caches, or fixing memory leaks"
            ))
        }
        
        // Check memory attribution violations
        metrics.memoryAttribution?.let { attribution ->
            val violation = evaluateMemoryAttribution(attribution, metrics.currentScreen)
            if (violation != null) {
                violations.add(violation)
            }
        }
        
        // Check frame attribution violations
        metrics.frameAttribution?.let { attribution ->
            val frameViolations = evaluateFrameAttribution(attribution, metrics.currentScreen)
            violations.addAll(frameViolations)
        }
        
        // Check frame rate budget
        if (metrics.frameRate < config.minFrameRate) {
            violations.add(createViolation(
                type = ViolationType.FRAME_RATE,
                screenName = metrics.currentScreen,
                actualValue = metrics.frameRate,
                budgetValue = config.minFrameRate,
                recommendation = "Optimize UI rendering, reduce overdraw, or move heavy operations off main thread"
            ))
        }
        
        // Check CPU usage budget
        if (metrics.cpuUsagePercent > config.maxCpuPercent) {
            violations.add(createViolation(
                type = ViolationType.CPU_USAGE,
                screenName = metrics.currentScreen,
                actualValue = metrics.cpuUsagePercent,
                budgetValue = config.maxCpuPercent,
                recommendation = "Optimize algorithms, use background threads, or reduce computational complexity"
            ))
        }
        
        // Check app size budget
        if (metrics.appSizeMb > config.maxAppSizeMb) {
            violations.add(createViolation(
                type = ViolationType.APP_SIZE,
                screenName = metrics.currentScreen,
                actualValue = metrics.appSizeMb,
                budgetValue = config.maxAppSizeMb,
                recommendation = "Remove unused resources, optimize images, or use dynamic delivery"
            ))
        }
        
        // Process violations (cooldown, history, notifications)
        val activeViolations = violations.filter { shouldReportViolation(it) }
        activeViolations.forEach { processViolation(it) }
        
        return activeViolations
    }
    
    private fun evaluateMemoryAttribution(attribution: MemoryAttribution, screenName: String): PerformanceViolation? {
        val memoryDelta = attribution.memoryDeltaMb
        if (memoryDelta <= 0) return null // Only check positive growth
        
        // Check screen delta budget
        if (memoryDelta > config.maxScreenDeltaMb) {
            return createViolation(
                type = ViolationType.SCREEN_MEMORY_DELTA,
                screenName = screenName,
                actualValue = memoryDelta,
                budgetValue = config.maxScreenDeltaMb,
                recommendation = "Investigate memory growth on this screen - check for leaks or excessive allocations",
                memoryAttribution = attribution
            )
        }
        
        // Check specific spike type budgets
        val (violationType, budget, recommendation) = when (attribution.likelyCause) {
            MemorySpike.BITMAP_HEAVY -> Triple(
                ViolationType.BITMAP_SPIKE,
                config.maxBitmapSpikeMb,
                "Reduce image resolution, implement image caching, or use more efficient image formats"
            )
            MemorySpike.COLLECTION_HEAVY -> Triple(
                ViolationType.COLLECTION_SPIKE,
                config.maxCollectionSpikeMb,
                "Optimize data structures, implement pagination, or use more memory-efficient collections"
            )
            MemorySpike.OBJECT_HEAVY -> Triple(
                ViolationType.OBJECT_SPIKE,
                config.maxObjectSpikeMb,
                "Reduce object creation, reuse objects, or optimize object lifecycle management"
            )
            MemorySpike.NATIVE_HEAVY -> Triple(
                ViolationType.NATIVE_SPIKE,
                config.maxNativeSpikeMb,
                "Review native library usage, check JNI memory management, or optimize native allocations"
            )
            else -> return null // No specific budget for NORMAL or UNKNOWN
        }
        
        if (memoryDelta > budget) {
            return createViolation(
                type = violationType,
                screenName = screenName,
                actualValue = memoryDelta,
                budgetValue = budget,
                recommendation = recommendation,
                memoryAttribution = attribution
            )
        }
        
        return null
    }
    
    private fun evaluateFrameAttribution(attribution: io.perfscope.sdk.data.FrameAttribution, screenName: String): List<PerformanceViolation> {
        val violations = mutableListOf<PerformanceViolation>()
        
        // Check jank percentage budget
        if (attribution.jankPercent > config.maxJankPercent) {
            violations.add(createViolation(
                type = ViolationType.JANK_PERCENT,
                screenName = screenName,
                actualValue = attribution.jankPercent.toInt(),
                budgetValue = config.maxJankPercent.toInt(),
                recommendation = "Reduce jank: ${attribution.jankType.description}. ${attribution.details}"
            ))
        }
        
        // Check average frame time budget
        if (attribution.averageFrameMs > config.maxFrameMs) {
            violations.add(createViolation(
                type = ViolationType.FRAME_TIME,
                screenName = screenName,
                actualValue = attribution.averageFrameMs.toInt(),
                budgetValue = config.maxFrameMs.toInt(),
                recommendation = "Optimize frame timing: ${attribution.jankType.description}. ${attribution.details}"
            ))
        }
        
        // Check for severe jank frames
        val maxFrameTime = attribution.averageFrameMs * 2 // Estimate worst frame from average
        if (maxFrameTime > config.maxSevereJankMs) {
            violations.add(createViolation(
                type = ViolationType.SEVERE_JANK,
                screenName = screenName,
                actualValue = maxFrameTime.toInt(),
                budgetValue = config.maxSevereJankMs.toInt(),
                recommendation = "Critical jank detected: ${attribution.jankType.description}. Immediate optimization needed."
            ))
        }
        
        return violations
    }
    
    private fun createViolation(
        type: ViolationType,
        screenName: String,
        actualValue: Int,
        budgetValue: Int,
        recommendation: String,
        memoryAttribution: MemoryAttribution? = null
    ): PerformanceViolation {
        return PerformanceViolation(
            type = type,
            screenName = screenName,
            actualValue = actualValue,
            budgetValue = budgetValue,
            severity = ViolationSeverity.fromExcess(actualValue, budgetValue),
            recommendation = recommendation,
            memoryAttribution = memoryAttribution
        )
    }
    
    private fun shouldReportViolation(violation: PerformanceViolation): Boolean {
        if (!config.enableViolationAlerts) return false
        
        val lastTime = lastViolationTime[violation.type] ?: 0
        val now = System.currentTimeMillis()
        
        return (now - lastTime) >= config.violationCooldownMs
    }
    
    private fun processViolation(violation: PerformanceViolation) {
        // Update last violation time
        lastViolationTime[violation.type] = violation.timestamp
        
        // Add to history if enabled
        if (config.trackViolationHistory) {
            val isRecurring = violationHistory.any { 
                it.violation.type == violation.type && 
                it.violation.screenName == violation.screenName &&
                it.resolvedAt == null
            }
            
            violationHistory.add(ViolationHistoryEntry(
                violation = violation,
                isRecurring = isRecurring
            ))
            
            // Keep history manageable
            if (violationHistory.size > 100) {
                violationHistory.removeAt(0)
            }
        }
        
        // Notify listeners
        violationListeners.forEach { it(violation) }
    }
    
    /**
     * Get current budget configuration
     */
    fun getConfig(): PerfScopeConfig = config
    
    /**
     * Update configuration
     */
    fun updateConfig(newConfig: PerfScopeConfig) {
        this.config = newConfig
    }
    
    /**
     * Add violation listener
     */
    fun addViolationListener(listener: (PerformanceViolation) -> Unit) {
        violationListeners.add(listener)
    }
    
    /**
     * Remove violation listener
     */
    fun removeViolationListener(listener: (PerformanceViolation) -> Unit) {
        violationListeners.remove(listener)
    }
    
    /**
     * Get violation history
     */
    fun getViolationHistory(): List<ViolationHistoryEntry> {
        return violationHistory.toList()
    }
    
    /**
     * Get active (unresolved) violations
     */
    fun getActiveViolations(): List<ViolationHistoryEntry> {
        return violationHistory.filter { it.resolvedAt == null }
    }
    
    /**
     * Mark violation as resolved
     */
    fun resolveViolation(violationType: ViolationType, screenName: String) {
        violationHistory.filter { 
            it.violation.type == violationType && 
            it.violation.screenName == screenName &&
            it.resolvedAt == null
        }.forEach { 
            it.copy(resolvedAt = System.currentTimeMillis())
        }
    }
}