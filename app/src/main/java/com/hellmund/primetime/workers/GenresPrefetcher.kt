package com.hellmund.primetime.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.hellmund.primetime.core.App
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.io.IOException
import javax.inject.Inject

private const val GENRES_WORKER_ID = "GENRES_WORKER_ID"

class GenresPrefetcher @Inject constructor(
    private val workManager: WorkManager
) {

    fun run() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<RefreshGenresWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(GENRES_WORKER_ID, ExistingWorkPolicy.REPLACE, request)
    }

    class RefreshGenresWorker(
        context: Context,
        workerParams: WorkerParameters
    ) : CoroutineWorker(context, workerParams) {

        @FlowPreview
        @ExperimentalCoroutinesApi
        override suspend fun doWork(): Result {
            val repository = App.coreComponent(applicationContext).genresRepository()
            return try {
                val apiGenres = repository.fetchGenres()
                repository.storeGenres(apiGenres)
                Result.success()
            } catch (e: IOException) {
                Result.retry()
            }
        }
    }
}
