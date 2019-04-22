package com.hellmund.primetime.model2

import com.google.gson.annotations.SerializedName
import com.hellmund.primetime.database.HistoryMovie
import com.hellmund.primetime.utils.Constants
import java.util.*

data class Sample(
        val id: Int,
        val title: String,
        @SerializedName("poster_path") val posterPath: String,
        val popularity: Double,
        val releaseDate: Date?,
        val selected: Boolean = false
) {

    val fullPosterUrl: String
        get() = "http://image.tmdb.org/t/p/w500$posterPath"

    fun toHistoryMovie(): HistoryMovie {
        return HistoryMovie(id, title, Constants.LIKE, Date(), false)
    }

}
