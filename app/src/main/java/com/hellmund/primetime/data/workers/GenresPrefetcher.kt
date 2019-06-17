package com.hellmund.primetime.data.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.RxWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.ui.selectgenres.GenresRepository
import io.reactivex.Single
import javax.inject.Inject

private const val GENRES_WORKER_ID = "GENRES_WORKER_ID"

class GenresPrefetcher @Inject constructor() {

    private val workManager: WorkManager = WorkManager.getInstance()

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
    ) : RxWorker(context, workerParams) {

        // TODO Constructor injection
        @Inject
        lateinit var genresRepository: GenresRepository

        override fun createWork(): Single<Result> {
            injector.inject(this)
            return Single.just(Result.success())

            /*return genresRepository
                    .all
                    .filter { it.isEmpty() }
                    .flatMapObservable { genresRepository.fetchGenres() }
                    .single(emptyList())
                    .flatMapCompletable { genresRepository.storeGenres(it) }
                    .andThen(Single.just(Result.success()))
                    .onErrorReturnItem(Result.retry())*/
        }

    }

}
