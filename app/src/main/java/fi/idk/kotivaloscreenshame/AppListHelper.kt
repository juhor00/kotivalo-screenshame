package fi.idk.kotivaloscreenshame

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

class AppHelper(private val context: Context) {
    fun getApps(): List<ApplicationInfo> {
        val packageManager: PackageManager = context.packageManager
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        // Exclude system apps
        return installedApps.filter { app ->
            // Exclude core system apps, but include updated system apps (e.g., YouTube)
            val isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdatedSystemApp = (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

            // Include only apps that either:
            // - are NOT system apps
            // - or are updated system apps (user-deletable)
            !isSystemApp || isUpdatedSystemApp
        }
    }

    fun getAppName(app: ApplicationInfo): String {
        val packageManager: PackageManager = context.packageManager
        val appName = packageManager.getApplicationLabel(app).toString()
        return appName
    }

    fun getAppIcon(app: ApplicationInfo): Drawable {
        val packageManager: PackageManager = context.packageManager
        val appIcon = packageManager.getApplicationIcon(app)
        return appIcon
    }
}