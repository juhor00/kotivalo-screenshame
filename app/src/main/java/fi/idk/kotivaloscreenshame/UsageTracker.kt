package fi.idk.kotivaloscreenshame

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.edit
import java.util.concurrent.TimeUnit

class UsageTracker(private val context: Context) {

    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val notifier = NotificationHelper(context)
    private val sharedPreferences =
        context.getSharedPreferences("tracked_apps", Context.MODE_PRIVATE)
    private val notifiedPrefs =
        context.getSharedPreferences("already_notified", Context.MODE_PRIVATE)

    private val thresholds = listOf(
        UsageThreshold(
            minutes = 1,
            windowMinutes = 10,
            notificationMessage = "👀 Spent over a minute in just 10 mins on %s!"
        ),
        UsageThreshold(
            minutes = 10,
            windowMinutes = 60,
            notificationMessage = "⌛ Over 10 minutes in the last hour on %s. Touch grass?"
        )
    )

    private val trackedPackages: Set<String>
        get() = sharedPreferences.getStringSet("selected_packages", emptySet()) ?: emptySet()

    suspend fun checkAppUsage() {
        Log.d("UsageTracker", "App usage...")
        val now = System.currentTimeMillis()

        thresholds.forEach { threshold ->

            Log.d("UsageTracker", "--Threshold: ${threshold.minutes} minutes within the last ${threshold.windowMinutes} minutes")
            val start = now - threshold.windowMinutes * 60 * 1000

            val stats: List<UsageStats> = usageStatsManager
                .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, now)
                .filter { it.totalTimeInForeground > 0 && trackedPackages.contains(it.packageName) }

            stats.forEach { usage ->

                checkStats(usage, threshold)

            }
        }
    }

    private fun checkStats(usage: UsageStats, threshold: UsageThreshold) {
        val hours = TimeUnit.MILLISECONDS.toHours(usage.totalTimeInForeground)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(usage.totalTimeInForeground) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(usage.totalTimeInForeground) % 60
        Log.d("UsageTracker", "App ${usage.packageName} usage: ${hours}h ${minutes}m ${seconds}s")

        val usedMinutes = usage.totalTimeInForeground / 60000
        if (usedMinutes >= threshold.minutes) {
            notifyAppUsage(usage.packageName, threshold)
        }
    }

    private fun notifyAppUsage(packageName: String, threshold: UsageThreshold) {
        val key = "${packageName}_${threshold.minutes}_${threshold.windowMinutes}"
        if (!hasAlreadyNotified(key)) {
            val appName = getApplicationName(packageName)
            val msg = String.format(threshold.notificationMessage, appName)

            Log.i("UsageTracker", msg)
            notifier.sendNaggyNotification(appName, msg)
            markNotified(key)
        } else {
            Log.d("UsageTracker", "Already notified for $key")
        }

    }

    suspend fun resetNotificationsForLowUsage() {
        Log.d("UsageTracker", "Resetting notifications for low usage...")
        val now = System.currentTimeMillis()

        thresholds.forEach { threshold ->
            val start = now - threshold.windowMinutes * 60 * 1000

            val stats: List<UsageStats> = usageStatsManager
                .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, now)
                .filter { trackedPackages.contains(it.packageName) }

            stats.forEach { usage ->
                val usedMinutes = usage.totalTimeInForeground / 60000
                if (usedMinutes < threshold.minutes) {
                    val key = "${usage.packageName}_${threshold.minutes}_${threshold.windowMinutes}"
                    if (hasAlreadyNotified(key)) {
                        Log.d("UsageTracker", "Resetting notification for $key due to low usage")
                        clearNotificationForKey(key)
                    }
                }
            }
        }
    }

    private fun clearNotificationForKey(key: String) {
        notifiedPrefs.edit { remove(key) }
    }

    private fun hasAlreadyNotified(key: String): Boolean {
        return notifiedPrefs.getBoolean(key, false)
    }

    private fun markNotified(key: String) {
        notifiedPrefs.edit() { putBoolean(key, true) }
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
