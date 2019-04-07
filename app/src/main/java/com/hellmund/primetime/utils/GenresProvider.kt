package com.hellmund.primetime.utils

import android.content.SharedPreferences

interface GenresProvider {
    fun getPreferredGenres(): List<String>
}

class RealGenresProvider(
        private val sharedPrefs: SharedPreferences
): GenresProvider {

    override fun getPreferredGenres(): List<String> {
        return sharedPrefs.getStringSet(Constants.KEY_INCLUDED, emptySet()).toList()
    }

}
