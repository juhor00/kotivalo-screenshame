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
        Handler(Looper.getMainLooper()).post {
            logUsageEvents()
        }
        return START_STICKY
    }

    private fun logUsageEvents() {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.MINUTE, -10) // Adjust the time range as needed
        val startTime = calendar.timeInMillis

        val usageEvents: UsageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            Log.i("AppUsageService", "Event: ${event.packageName}, " +
                    "Type: ${event.eventType}, " +
                    "Time: ${event.timeStamp}")
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