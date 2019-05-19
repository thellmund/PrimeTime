package com.hellmund.primetime.ui.selectgenres

import com.hellmund.primetime.data.api.ApiService
import com.hellmund.primetime.data.database.AppDatabase
import com.hellmund.primetime.data.model.Genre
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GenresRepository @Inject constructor(
        private val apiService: ApiService,
        private val database: AppDatabase
) {

    val all: Single<List<Genre>>
        get() = database.genreDao()
                .getAll()
                .subscribeOn(Schedulers.io())

    val preferredGenres: Observable<List<Genre>>
        get() = database.genreDao()
                .getPreferredGenres()
                .subscribeOn(Schedulers.io())
                .toObservable()

    val excludedGenres: Observable<List<Genre>>
        get() = database.genreDao()
                .getExcludedGenres()
                .subscribeOn(Schedulers.io())
                .toObservable()

    fun fetchGenres(): Observable<List<Genre>> {
        return apiService.genres()
                .subscribeOn(Schedulers.io())
                .map { it.genres }
                .map { it.map { genre -> Genre(genre.id, genre.name) } }
    }

    fun getGenre(genreId: String): Maybe<Genre> = database.genreDao()
            .getGenre(genreId.toInt())
            .subscribeOn(Schedulers.io())

    fun getGenreByName(name: String): Maybe<Genre> = database.genreDao()
            .getGenre(name)
            .subscribeOn(Schedulers.io())

    fun getGenres(genreIds: Set<String>): Single<List<Genre>> {
        return Single
                .fromCallable {
                    genreIds.map { database.genreDao().getGenre(it.toInt()).blockingGet() }
                }
                .subscribeOn(Schedulers.io())
    }

    fun storeGenres(genres: List<Genre>): Completable {
        return database.genreDao()
                .store(*genres.toTypedArray())
                .subscribeOn(Schedulers.io())
    }

}
