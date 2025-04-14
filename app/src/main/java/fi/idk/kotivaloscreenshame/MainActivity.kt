package fi.idk.kotivaloscreenshame

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission()
        }
        // Schedule anyways
        scheduleTracking()
        scheduleNotificationCleanup()

        findViewById<Button>(R.id.chooseAppsButton).setOnClickListener {
            val intent = Intent(this, AppSelectionActivity::class.java)
            startActivity(intent)
        }

        val debugButton = findViewById<Button>(R.id.debugButton)
        debugButton.setOnClickListener {
            val debugRequest = OneTimeWorkRequestBuilder<AppUsageWorker>().build()
            WorkManager.getInstance(this).enqueue(debugRequest)
        }

        val resetNotificationsButton = findViewById<Button>(R.id.resetNotificationButton)
        resetNotificationsButton.setOnClickListener {
            val usageTracker = UsageTracker(this)
            usageTracker.clearNotifications()

            // TODO: Move to Kotivalo class or something
            val messages = listOf(
                "Hey! Where’d your doomscrolling go? I was enjoying the show!",
                "Whoa, no social media? Are you okay?? 😱",
                "You’ve ghosted your apps... proud of you 👏",
                "Silence... too quiet. Are you hiding from me?",
                "No screen time? Who even *are* you?",
                "I blinked and your screen time vanished. Witchcraft?",
                "Less scrolling, more strolling. Touching grass detected 🌱",
                "My data says you’ve been suspiciously offline...",
                "Hey, come back. I was just about to judge you.",
                "Rest in peace, doomscroll session. Gone too soon. 🪦"
            )

            val randomMessage = messages.random()

            Toast.makeText(this, randomMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageStatsPermission() {
        Toast.makeText(this, "Please grant usage access permission", Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
    }

    private fun scheduleTracking() {
        val workRequest = PeriodicWorkRequestBuilder<AppUsageWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "AppUsageTracking",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun scheduleNotificationCleanup() {
        val now = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, 1) // Move to the next day
        }

        val initialDelay = midnight.timeInMillis - now.timeInMillis

        val cleanupRequest = PeriodicWorkRequestBuilder<NotificationResetWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "NotificationReset",
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupRequest
        )
    }
}