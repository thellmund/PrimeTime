package com.hellmund.primetime.ui.selectgenres

import com.hellmund.primetime.data.api.ApiService
import com.hellmund.primetime.data.database.AppDatabase
import com.hellmund.primetime.data.model.Genre
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

interface GenresRepository {
    suspend fun getAll(): List<Genre>
    val all: Single<List<Genre>>
    suspend fun getPreferredGenres(): List<Genre>
    suspend fun getExcludedGenres(): List<Genre>
    suspend fun fetchGenres(): List<Genre>
    fun getGenre(genreId: String): Maybe<Genre>
    fun getGenreByName(name: String): Maybe<Genre>
    fun getGenres(genreIds: Set<String>): Single<List<Genre>>
    suspend fun storeGenres(genres: List<Genre>)
}

class RealGenresRepository @Inject constructor(
        private val apiService: ApiService,
        private val database: AppDatabase
) : GenresRepository {

    override suspend fun getAll(): List<Genre> {
        return database.genreDao().getAll()
    }

    override val all: Single<List<Genre>>
        get() = database.genreDao()
                .getAllRx()
                .subscribeOn(Schedulers.io())

    override suspend fun getPreferredGenres() = database.genreDao().getPreferredGenres()

    override suspend fun getExcludedGenres() = database.genreDao().getExcludedGenres()

    override suspend fun fetchGenres(): List<Genre> {
        val genres = apiService.genres()
        return genres.genres.map { Genre(it.id, it.name) }
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

    override suspend fun storeGenres(genres: List<Genre>) {
        database.genreDao().store(*genres.toTypedArray())
    }

}
