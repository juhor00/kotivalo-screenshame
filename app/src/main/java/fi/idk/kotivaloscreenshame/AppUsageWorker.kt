package fi.idk.kotivaloscreenshame

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class AppUsageWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val usageTracker = UsageTracker(applicationContext)

        usageTracker.resetNotificationsForLowUsage()
        usageTracker.checkAppUsage()

        return Result.success()
    }
}
