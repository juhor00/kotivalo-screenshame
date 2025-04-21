package fi.idk.kotivaloscreenshame

import android.app.usage.UsageEvents
import android.app.usage.UsageEventsQuery
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.util.Log
import androidx.core.content.edit

class UsageTracker(private val context: Context) {

    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val notifier = NotificationHelper(context)
    private val trackPreferences =
        context.getSharedPreferences("track_preferences", Context.MODE_PRIVATE)
    private val alreadyNotifiedKey = "already_notified"
    private val lastResetTimeKey = "last_reset_time"
    private val selectedPackagesKey = "selected_packages"
    private val debugEnabledKey = "debug_enabled"

    private enum class NotifyType {
        NOTIFIED,
        ALREADY_NOTIFIED,
        NOT_NOTIFIED
    }

    private val singleAppThresholds = listOf(
        UsageThreshold(
            severity = 1,
            minutes = 20
        ),
        UsageThreshold(
            severity = 2,
            minutes = 40
        ),
        UsageThreshold(
            severity = 3,
            minutes = 60
        ),
        UsageThreshold(
            severity = 4,
            minutes = 90
        ),
        UsageThreshold(
            severity = 5,
            minutes = 120
        )
    )

    private val multiAppThresholds = listOf(
        UsageThreshold(
            severity = 1,
            minutes = 45
        ),
        UsageThreshold(
            severity = 2,
            minutes = 90
        ),
        UsageThreshold(
            severity = 3,
            minutes = 120
        ),
        UsageThreshold(
            severity = 4,
            minutes = 180
        ),
        UsageThreshold(
            severity = 5,
            minutes = 240
        )
    )

    private val trackedPackages: Set<String>
        get() = trackPreferences.getStringSet(selectedPackagesKey, emptySet()) ?: emptySet()

    fun checkAppUsage() {
        Log.d("UsageTracker", "App usage...")
        val isMockEnabled = trackPreferences.getBoolean("mock_enabled", false)
        val usageStatsList: List<UsageStats> = if (isMockEnabled) getMockStats() else getUsageStats() ?: return
        logUsageStats(usageStatsList)

        if (isDebugEnabled()) {
            Log.d("UsageTracker", "Debug mode enabled, sending debug notification.")
            notifier.sendDebugNotification(usageStatsList, getLastNotifiedSeverity())
        }
        val notifiedAny = checkSingleAppUsageThresholds(usageStatsList)
        if (!notifiedAny) {
            checkCombinedUsageThresholds(usageStatsList)
        }
    }

    private fun getUsageStats(): List<UsageStats>? {
        val calendar = Calendar.getInstance()
        val endTime: Long = calendar.timeInMillis
        val startTime: Long = getLastResetTime()

        val query: UsageEventsQuery = UsageEventsQuery.Builder(startTime, endTime)
            .setPackageNames(*trackedPackages.toTypedArray())
            .setEventTypes(UsageEvents.Event.ACTIVITY_RESUMED, UsageEvents.Event.ACTIVITY_PAUSED)
            .build()
        val usageEvents: UsageEvents = usageStatsManager.queryEvents(query) ?: return null
         return constructUsageStats(usageEvents)
    }

    private fun getMockStats(): List<UsageStats> {
        val mockTime = trackPreferences.getInt("mock_time", 0)
        val mockStats = listOf(
            UsageStats(
                firstTimeStamp = 0,
                lastTimeStamp = 0,
                totalTimeInForeground = mockTime * 60 * 1000L,
                packageName = "com.example.mockapp"
            )
        )
        return mockStats
    }

    private fun constructUsageStats(usageEvents: UsageEvents): List<UsageStats> {

        val appStatsMap: MutableMap<String, UsageStats> = HashMap<String, UsageStats>()

        val event = UsageEvents.Event()
        val foregroundStartMap: MutableMap<String, Long> = HashMap()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            val packageName = event.packageName

            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> foregroundStartMap[packageName] = event.timeStamp

                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    val startTime = foregroundStartMap.remove(packageName)
                    if (startTime != null) {
                        val duration = event.timeStamp - startTime
                        val oldStats: UsageStats = appStatsMap.getOrDefault(packageName, UsageStats(0, 0, 0, packageName))
                        val newStats = UsageStats(
                            firstTimeStamp = if (oldStats.firstTimeStamp == 0L) startTime else oldStats.firstTimeStamp,
                            lastTimeStamp = oldStats.lastTimeStamp.coerceAtLeast(event.timeStamp),
                            totalTimeInForeground = oldStats.totalTimeInForeground + duration,
                            packageName = packageName
                        )
                        appStatsMap[packageName] = newStats
                    }
                }
            }
        }
        return appStatsMap.values.toList()

    }

    private fun logUsageStats(usageStatsList: List<UsageStats>) {

        Log.d("UsageTracker", "Logging usage stats:")
        for (usageStats in usageStatsList) {

            val totalTimeSeconds = usageStats.totalTimeInForeground / 1000 // Convert to seconds
            val hours = totalTimeSeconds / 3600
            val minutes = (totalTimeSeconds % 3600) / 60
            val seconds = totalTimeSeconds % 60

            val firstTime = (Calendar.getInstance().timeInMillis - usageStats.firstTimeStamp) / 60000
            val lastTime = (Calendar.getInstance().timeInMillis - usageStats.lastTimeStamp) / 60000

            Log.d(
                "UsageTracker",
                "Package: ${usageStats.packageName}, First Time: $firstTime minutes ago, Last Time: $lastTime minutes ago, Total Time: $hours hours, $minutes minutes, $seconds seconds"
            )
        }
    }

    private fun checkCombinedUsageThresholds(
        allUsageStats: List<UsageStats>
    ): Boolean {
        for (threshold in multiAppThresholds.reversed()) {
            val result = notifyCombinedAppUsage(allUsageStats, threshold)
            if (result == NotifyType.NOTIFIED) return true
            if (result == NotifyType.ALREADY_NOTIFIED) return false
            // Continue if NOT_NOTIFIED
        }
        return false // If all NOT_NOTIFIED, return false
    }

    private fun checkSingleAppUsageThresholds(
        allUsageStats: List<UsageStats>
    ): Boolean {
        // Get the most used app
        val mostUsedApp = allUsageStats.maxByOrNull { it.totalTimeInForeground }
        if (mostUsedApp == null) {
            return false
        }
        for (threshold in singleAppThresholds.reversed()) {
            val result = notifyAppUsage(mostUsedApp, threshold)
            if (result == NotifyType.NOTIFIED) return true
            if (result == NotifyType.ALREADY_NOTIFIED) return false
            // Continue if NOT_NOTIFIED
        }
        return false // If all NOT_NOTIFIED, return false
    }

    private fun notifyCombinedAppUsage(allUsageStats: List<UsageStats>, threshold: UsageThreshold): NotifyType {
        if (getLastNotifiedSeverity() >= threshold.severity) {
            Log.d("UsageTracker", "Do not notify combined app usage. Already notified for severity ${threshold.severity}.")
            return NotifyType.ALREADY_NOTIFIED
        }

        val totalUsageMinutes = allUsageStats.sumOf { it.totalTimeInForeground } / 60000
        if (totalUsageMinutes < threshold.minutes) {
            return NotifyType.NOT_NOTIFIED
        }

        val appNames = allUsageStats.map { it.packageName }.map { getApplicationName(it) }
        Log.i("UsageTracker", "Notify combined app usage for severity ${threshold.severity}")
        notifier.sendCombinedNotification(appNames, threshold.severity, totalUsageMinutes)
        markNotified(threshold.severity)
        return NotifyType.NOTIFIED

    }

    private fun notifyAppUsage(usage: UsageStats, threshold: UsageThreshold): NotifyType {
        if (getLastNotifiedSeverity() >= threshold.severity) {
            Log.d("UsageTracker", "Do not notify app usage. Already notified for severity ${threshold.severity}.")
            return NotifyType.ALREADY_NOTIFIED
        }
        val usedMinutes = usage.totalTimeInForeground / 60000
        if (usedMinutes < threshold.minutes) {
           return NotifyType.NOT_NOTIFIED
        }
        val appName = getApplicationName(usage.packageName)
        Log.i("UsageTracker", "Notify app usage for severity ${threshold.severity}")
        notifier.sendNotificationForApp(appName, threshold.severity, usedMinutes)
        markNotified(threshold.severity)
        return NotifyType.NOTIFIED
    }

    private fun isDebugEnabled(): Boolean {
        return trackPreferences.getBoolean(debugEnabledKey, false)
    }

    private fun getLastNotifiedSeverity(): Int {
        if (trackPreferences.getBoolean("mock_enabled", false)) {
            return trackPreferences.getInt("mock_severity", 0)
        }
        return trackPreferences.getInt(alreadyNotifiedKey, 0)
    }

    private fun markNotified(severity: Int) {
        if (trackPreferences.getBoolean("mock_enabled", false)) {
            return
        }
        trackPreferences.edit() { putInt(alreadyNotifiedKey, severity) }
    }

    fun clearNotifications() {
        Log.d("UsageTracker", "Clearing all notifications...")
        trackPreferences.edit { putInt(alreadyNotifiedKey, 0) }
        trackPreferences.edit { putLong(lastResetTimeKey, System.currentTimeMillis()) }
    }

    private fun getLastResetTime(): Long {
        val lastResetTime = trackPreferences.getLong(lastResetTimeKey, 0)
        if (lastResetTime == 0L) {
            val currentTime = System.currentTimeMillis()
            trackPreferences.edit { putLong(lastResetTimeKey, currentTime) }
            return currentTime
        }
        return lastResetTime
    }

    private fun getApplicationName(packageName: String): String {
        val packageManager = context.packageManager
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("UsageTracker", "Package not found: $packageName", e)
            "Unknown App"
        }
    }

}
