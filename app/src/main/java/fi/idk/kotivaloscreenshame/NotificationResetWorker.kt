package fi.idk.kotivaloscreenshame

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class NotificationResetWorker (
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val usageTracker = UsageTracker(applicationContext)
        usageTracker.clearNotifications()

        return Result.success()
    }
}
