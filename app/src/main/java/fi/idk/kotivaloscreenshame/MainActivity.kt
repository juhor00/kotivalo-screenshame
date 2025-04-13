package fi.idk.kotivaloscreenshame

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
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
        } else {
            startAppUsageWork()
        }

        findViewById<Button>(R.id.chooseAppsButton).setOnClickListener {
            val intent = Intent(this, AppSelectionActivity::class.java)
            startActivity(intent)
        }



        val debugButton = findViewById<Button>(R.id.debugButton)
        debugButton.setOnClickListener {
            val debugRequest = OneTimeWorkRequestBuilder<AppUsageWorker>().build()
            WorkManager.getInstance(this).enqueue(debugRequest)
        }

        val clearHistoryButton = findViewById<Button>(R.id.clearHistoryButton)
        clearHistoryButton.setOnClickListener {
            val usageTracker = UsageTracker(this)
            usageTracker.clearNotifications()

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

    override fun onResume() {
        super.onResume()
        if (hasUsageStatsPermission()) {
            startAppUsageWork()
        }
    }

    private fun startAppUsageWork() {

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
}