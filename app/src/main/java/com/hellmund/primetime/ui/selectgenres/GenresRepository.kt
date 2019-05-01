package com.hellmund.primetime.ui.selectgenres

import com.hellmund.primetime.data.api.ApiService
import com.hellmund.primetime.data.database.AppDatabase
import com.hellmund.primetime.data.model.Genre
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class GenresRepository @Inject constructor(
        private val apiService: ApiService,
        private val database: AppDatabase
) {

    val all: Single<List<Genre>>
        get() = database.genreDao().getAll()

    val preferredGenres: Observable<List<Genre>>
        get() = database.genreDao().getPreferredGenres().toObservable()

    val excludedGenres: Observable<List<Genre>>
        get() = database.genreDao().getExcludedGenres().toObservable()

    fun fetchGenres(): Observable<List<Genre>> {
        return apiService.genres()
                .map { it.genres }
                .map { it.map { genre -> Genre(genre.id, genre.name) } }
    }

    fun getGenre(genreId: String): Maybe<Genre> = database.genreDao().getGenre(genreId.toInt())

    fun storeGenres(genres: List<Genre>) {
        database.genreDao().store(*genres.toTypedArray())
    }

}
