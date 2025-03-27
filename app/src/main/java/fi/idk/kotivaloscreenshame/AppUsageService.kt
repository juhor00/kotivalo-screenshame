package fi.idk.kotivaloscreenshame

import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.IBinder

class AppUsageService : Service() {

    private lateinit var usageStatsManager: UsageStatsManager

    override fun onCreate() {
        super.onCreate()
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Implement tracking logic here
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}