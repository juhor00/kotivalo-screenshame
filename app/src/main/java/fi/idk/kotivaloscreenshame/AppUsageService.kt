package fi.idk.kotivaloscreenshame

import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import java.util.Calendar

class AppUsageService : Service() {

    private lateinit var usageStatsManager: UsageStatsManager

    override fun onCreate() {
        super.onCreate()
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logApplications()
        Handler(Looper.getMainLooper()).post {
            logUsageEvents()
        }
        return START_STICKY
    }

    private fun logApplications() {
        val appHelper = AppHelper(this)
        val apps = appHelper.getApps()

        for (app in apps) {
            val label = appHelper.getAppName(app)
            val packageName = app.packageName
            Log.i("AppUsageService", "App: $label, Package: $packageName")
        }
    }

    private fun logUsageEvents() {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.MINUTE, -10) // Adjust the time range as needed
        val startTime = calendar.timeInMillis

        val usageEvents: UsageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()

        val appUsageMap = mutableMapOf<String, Long>()
        val foregroundTimes = mutableMapOf<String, Long>()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    foregroundTimes[event.packageName] = event.timeStamp
                }

                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    val start = foregroundTimes[event.packageName]
                    if (start != null) {
                        val duration = event.timeStamp - start
                        appUsageMap[event.packageName] = appUsageMap.getOrDefault(event.packageName, 0L) + duration
                        foregroundTimes.remove(event.packageName)
                    }
                }
            }
        }

        val usageThreshold = 1 * 60 * 1000L // 5 minutes in milliseconds

        for ((packageName, usageTime) in appUsageMap) {
            if (usageTime > usageThreshold) {
                Log.i("AppUsageService", "App $packageName exceeded usage limit: ${usageTime / 60000} minutes in last 10 minutes")
            }
        }

        // Schedule the next log
        Handler(Looper.getMainLooper()).postDelayed({
            logUsageEvents()
        }, 60000) // Adjust the interval as needed
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}