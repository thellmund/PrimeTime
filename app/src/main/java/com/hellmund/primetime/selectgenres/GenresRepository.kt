package com.hellmund.primetime.selectgenres

import android.content.SharedPreferences
import com.hellmund.primetime.api.ApiService
import com.hellmund.primetime.model.Genre
import com.hellmund.primetime.utils.Constants
import io.reactivex.Observable

class GenresRepository(
        private val apiService: ApiService,
        private val sharedPrefs: SharedPreferences
) {

    fun fetchGenres(): Observable<List<Genre>> {
        return apiService.genres().map { it.genres }
    }

    fun storeGenres(genres: Set<String>) {
        sharedPrefs
                .edit()
                .putStringSet(Constants.KEY_INCLUDED, genres)
                .apply()
    }

}
