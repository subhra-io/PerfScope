package io.perfscope.sdk.tracking

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

/**
 * Tracks current screen/activity for memory attribution
 */
class ScreenTracker private constructor() {
    
    private var currentScreen: String = "Unknown"
    private var currentActivity: WeakReference<Activity>? = null
    private val screenChangeListeners = mutableListOf<(String) -> Unit>()
    
    companion object {
        @Volatile
        private var INSTANCE: ScreenTracker? = null
        
        fun getInstance(): ScreenTracker {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ScreenTracker().also { INSTANCE = it }
            }
        }
    }
    
    fun initialize(application: Application) {
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                updateCurrentScreen(activity)
            }
            
            override fun onActivityStarted(activity: Activity) {
                updateCurrentScreen(activity)
            }
            
            override fun onActivityResumed(activity: Activity) {
                updateCurrentScreen(activity)
            }
            
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
    
    private fun updateCurrentScreen(activity: Activity) {
        currentActivity = WeakReference(activity)
        val newScreen = getScreenName(activity)
        if (newScreen != currentScreen) {
            currentScreen = newScreen
            notifyScreenChange(newScreen)
        }
    }
    
    private fun getScreenName(activity: Activity): String {
        return activity.javaClass.simpleName.replace("Activity", "")
    }
    
    fun getCurrentScreen(): String = currentScreen
    
    fun addScreenChangeListener(listener: (String) -> Unit) {
        screenChangeListeners.add(listener)
    }
    
    fun removeScreenChangeListener(listener: (String) -> Unit) {
        screenChangeListeners.remove(listener)
    }
    
    private fun notifyScreenChange(screenName: String) {
        screenChangeListeners.forEach { it(screenName) }
    }
    
    /**
     * Manual screen tracking for Compose screens or custom naming
     */
    fun setCurrentScreen(screenName: String) {
        if (screenName != currentScreen) {
            currentScreen = screenName
            notifyScreenChange(screenName)
        }
    }
}