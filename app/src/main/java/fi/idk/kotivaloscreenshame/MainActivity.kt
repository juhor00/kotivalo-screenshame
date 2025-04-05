package fi.idk.kotivaloscreenshame

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission()
        } else {
            startAppUsageService()
        }

        findViewById<Button>(R.id.chooseAppsButton).setOnClickListener {
            val intent = Intent(this, AppSelectionActivity::class.java)
            startActivity(intent)
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
            startAppUsageService()
        }
    }

    private fun startAppUsageService() {
        val serviceIntent = Intent(this, AppUsageService::class.java)
        startService(serviceIntent)
    }
}