package com.hellmund.primetime.ui.selectmovies

import com.google.gson.annotations.SerializedName
import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.utils.Constants
import org.threeten.bp.LocalDate
import java.util.*

data class SamplesResponse(val results: List<Sample>)

data class Sample(
        val id: Int,
        val title: String,
        @SerializedName("poster_path") val posterPath: String,
        val popularity: Double,
        val releaseDate: Date?,
        val selected: Boolean = false
) {

    val fullPosterUrl: String
        get() = "https://image.tmdb.org/t/p/w500$posterPath"

    fun toHistoryMovie(): HistoryMovie {
        return HistoryMovie(id, title, Constants.LIKE, LocalDate.now())
    }

}
