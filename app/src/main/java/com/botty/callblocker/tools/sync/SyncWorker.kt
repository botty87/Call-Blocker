package com.botty.callblocker.tools.sync

import android.content.Context
import androidx.work.*
import com.botty.callblocker.MainActivity
import com.botty.callblocker.data.DB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return DB.sync(applicationContext)
    }
}

private const val TAG = "sync_job"

fun MainActivity.setSyncWorker() {
    val const = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.METERED)
        .setRequiresBatteryNotLow(true)
        .build()

    val workReq = PeriodicWorkRequestBuilder<SyncWorker>(2, TimeUnit.HOURS, 20, TimeUnit.MINUTES)
        .addTag(TAG)
        .setConstraints(const)
        .build()

    WorkManager.getInstance(this).enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.KEEP, workReq)

    launch(Dispatchers.IO) {
        DB.sync(this@setSyncWorker)
    }
}