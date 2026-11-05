package com.lifeindex.app.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lifeindex.app.data.local.SessionManager
import com.lifeindex.app.data.remote.RetrofitClient
import com.lifeindex.app.data.repository.TrackerRepository

class TrackerSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
            RetrofitClient.initialize(applicationContext)
            val session = SessionManager(applicationContext)
            val token = session.getAccessToken() ?: return Result.success()
            val userId = session.getUserId() ?: return Result.success()
            val repository = TrackerRepository(applicationContext)
            val pushed = repository.syncPending(userId, token)
            val pulled = repository.refreshTrackers(userId, token)
            if (pushed && pulled) Result.success() else Result.retry()
        } catch (error: Exception) {
            Log.e("LifeIndexSync", "Tracker sync failed; WorkManager will retry", error)
            Result.retry()
        }
    }
}