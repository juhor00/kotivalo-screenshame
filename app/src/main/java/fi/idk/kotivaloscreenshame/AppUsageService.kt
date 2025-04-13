package fi.idk.kotivaloscreenshame

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.*

class AppUsageService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var usageTracker: UsageTracker
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate() {
        super.onCreate()
        usageTracker = UsageTracker(this)
        notificationHelper = NotificationHelper(this)
        startForegroundService()
        startTrackingLoop()
        startCleanupLoop()
    }

    private fun startTrackingLoop() {
        serviceScope.launch {
            while (isActive) {
                usageTracker.checkAppUsage()
                delay(60000) // check every minute
            }
        }
    }

    private fun startCleanupLoop() {
        serviceScope.launch {
            while (isActive) {
                usageTracker.resetNotificationsForLowUsage()
                delay(60000) // check every minute
            }
        }
    }

    private fun startForegroundService() {
        val notification = notificationHelper.createTrackerNotification()
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}