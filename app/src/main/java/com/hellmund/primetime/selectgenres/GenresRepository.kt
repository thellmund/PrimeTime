package com.hellmund.primetime.selectgenres

import com.hellmund.primetime.api.ApiService
import com.hellmund.primetime.database.AppDatabase
import com.hellmund.primetime.model2.Genre
import io.reactivex.Observable

class GenresRepository(
        private val apiService: ApiService,
        private val database: AppDatabase
) {

    val preferredGenres: Observable<List<Genre>>
        get() = database.genreDao().getPreferredGenres().toObservable()

    fun fetchGenres(): Observable<List<Genre>> {
        return apiService.genres()
                .map { it.genres }
                .map { it.map { genre -> Genre(genre.id, genre.name) } }
    }

    fun storeGenres(genres: List<Genre>) {
        database.genreDao().store(*genres.toTypedArray())
        /*sharedPrefs
                .edit()
                .putStringSet(Constants.KEY_INCLUDED, genres)
                .apply()*/
    }

}
