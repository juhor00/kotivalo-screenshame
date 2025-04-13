package fi.idk.kotivaloscreenshame


import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.TimeUnit
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {

    private val channelId = "kotivalo-nagger"
    private val channelName = "Kotivalo Nagger"

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun createTrackerNotification(): Notification {
        val channelId = "kotivalo-tracker"
        val channelName = "Kotivalo Tracker"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(context, channelId)
            .setContentTitle("Kotivalo is judging you!")
            .setContentText("Watching your app usage 👀")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .build()
    }

    fun sendNaggyNotification(appName: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("NotificationHelper", "Missing POST_NOTIFICATIONS permission, can't notify.")
            return
        }

        val bigImage = BitmapFactory.decodeResource(context.resources, R.drawable.kotivalo4_icon)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Kotivalo is not pleased!")
            .setContentText(message)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(bigImage))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(appName.hashCode(), notification)
    }
}