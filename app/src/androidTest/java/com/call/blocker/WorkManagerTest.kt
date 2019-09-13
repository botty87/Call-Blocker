package com.call.blocker

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.*
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.call.blocker.tools.sync.SyncWorker
import org.hamcrest.CoreMatchers.`is`

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before

@RunWith(AndroidJUnit4::class)
class WorkManagerTest {
    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    @Throws(Exception::class)
    fun testSyncWorker() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .build()

        val workManager = WorkManager.getInstance(context)
        // Enqueue and wait for result. This also runs the Worker synchronously
        // because we are using a SynchronousExecutor.
        workManager.enqueue(request).result.get()
        // Get WorkInfo and outputData
        val workInfo = workManager.getWorkInfoById(request.id).get()
        // Assert
        assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))
    }

}
