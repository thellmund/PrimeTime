package com.hellmund.primetime.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.hellmund.primetime.data.repositories.GenresRepository
import com.hellmund.primetime.di.injector
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

        // TODO Constructor injection
        @Inject
        lateinit var genresRepository: GenresRepository

        @FlowPreview
        @ExperimentalCoroutinesApi
        override suspend fun doWork(): Result {
            injector.inject(this)

            val genres = genresRepository.getAll()
            if (genres.isNotEmpty()) {
                return Result.success()
            }

            return try {
                val apiGenres = genresRepository.fetchGenres()
                genresRepository.storeGenres(apiGenres)
                Result.success()
            } catch (e: IOException) {
                Result.retry()
            }
        }

    }

}
