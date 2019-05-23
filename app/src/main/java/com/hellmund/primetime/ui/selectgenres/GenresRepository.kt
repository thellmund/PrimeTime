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

interface GenresRepository {
    val all: Single<List<Genre>>
    val preferredGenres: Observable<List<Genre>>
    val excludedGenres: Observable<List<Genre>>
    fun fetchGenres(): Observable<List<Genre>>
    fun getGenre(genreId: String): Maybe<Genre>
    fun getGenreByName(name: String): Maybe<Genre>
    fun getGenres(genreIds: Set<String>): Single<List<Genre>>
    fun storeGenres(genres: List<Genre>): Completable
}

class RealGenresRepository @Inject constructor(
        private val apiService: ApiService,
        private val database: AppDatabase
) : GenresRepository {

    override val all: Single<List<Genre>>
        get() = database.genreDao()
                .getAll()
                .subscribeOn(Schedulers.io())

    override val preferredGenres: Observable<List<Genre>>
        get() = database.genreDao()
                .getPreferredGenres()
                .subscribeOn(Schedulers.io())
                .toObservable()

    override val excludedGenres: Observable<List<Genre>>
        get() = database.genreDao()
                .getExcludedGenres()
                .subscribeOn(Schedulers.io())
                .toObservable()

    override fun fetchGenres(): Observable<List<Genre>> {
        return apiService.genres()
                .subscribeOn(Schedulers.io())
                .map { it.genres }
                .map { it.map { genre -> Genre(genre.id, genre.name) } }
    }

    override fun getGenre(genreId: String): Maybe<Genre> = database.genreDao()
            .getGenre(genreId.toInt())
            .subscribeOn(Schedulers.io())

    override fun getGenreByName(name: String): Maybe<Genre> = database.genreDao()
            .getGenre(name)
            .subscribeOn(Schedulers.io())

    override fun getGenres(genreIds: Set<String>): Single<List<Genre>> {
        return Single
                .fromCallable {
                    genreIds.map { database.genreDao().getGenre(it.toInt()).blockingGet() }
                }
                .subscribeOn(Schedulers.io())
    }

    override fun storeGenres(genres: List<Genre>): Completable {
        return database.genreDao()
                .store(*genres.toTypedArray())
                .subscribeOn(Schedulers.io())
    }

}
