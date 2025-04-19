package fi.idk.kotivaloscreenshame


import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {

    private val channelId = "kotivalo-channel"
    private val channelName = "Kotivalo Channel"

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun sendNotificationForApp(appName: String, severity: Int, minutes: Long) {
        val message = when (severity) {
            1 -> "You have used $appName for $minutes minutes. Kotivalo is not pleased!"
            2 -> "You have used $appName for $minutes minutes. Kotivalo is very displeased!"
            3 -> "You have used $appName for $minutes minutes. Kotivalo is extremely displeased!"
            4 -> "You have used $appName for $minutes minutes. Kotivalo is furious!"
            5 -> "You have used $appName for $minutes minutes. Kotivalo is enraged!"
            else -> "You have used $appName for $minutes minutes. Kotivalo is not pleased!"
        }
        sendNotification(appName.hashCode(), "Kotivalo is not pleased!", message, R.drawable.kotivalo4_icon, android.R.drawable.ic_dialog_alert)
    }

    fun sendCombinedNotification(appNames: List<String>, severity: Int, totalMinutes: Long) {
        val message = when (severity) {
            1 -> "You have used ${appNames.size} apps for a total of $totalMinutes minutes. Kotivalo is not pleased!"
            2 -> "You have used ${appNames.size} apps for a total of $totalMinutes minutes. Kotivalo is very displeased!"
            3 -> "You have used ${appNames.size} apps for a total of $totalMinutes minutes. Kotivalo is extremely displeased!"
            4 -> "You have used ${appNames.size} apps for a total of $totalMinutes minutes. Kotivalo is furious!"
            5 -> "You have used ${appNames.size} apps for a total of $totalMinutes minutes. Kotivalo is enraged!"
            else -> "You have used ${appNames.size} apps for a total of $totalMinutes minutes. Kotivalo is not pleased!"
        }
        // Create the notification with the combined message
        sendNotification(appNames.hashCode(), "Kotivalo is not pleased!", message, R.drawable.kotivalo4_icon, android.R.drawable.ic_dialog_alert)
    }

    fun sendDebugNotification(usageStats: List<UsageStats>, latestSeverity: Int) {
        val maxTime = (usageStats.maxOfOrNull { it.totalTimeInForeground } ?: 0) / (1000 * 60)
        val totalMinutes = usageStats.sumOf { it.totalTimeInForeground } / (1000 * 60)

        val message = "Debug Notification: Max time: $maxTime, Total time: $totalMinutes, Severity: $latestSeverity"
        // Random notification ID for debug purposes
        val notificationId = (0..99999999999).random()
        sendNotification(notificationId.toInt(), "Debug Notification", message, R.drawable.kotivalo12, android.R.drawable.ic_dialog_alert)
    }

    private fun sendNotification(notificationId: Int, title: String, message: String, image: Int, icon: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("NotificationHelper", "Missing POST_NOTIFICATIONS permission, can't notify.")
            return
        }

        val bigImage = BitmapFactory.decodeResource(context.resources, image)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(bigImage))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}