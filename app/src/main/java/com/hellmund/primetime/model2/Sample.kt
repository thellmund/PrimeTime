package com.hellmund.primetime.model2

import com.google.gson.annotations.SerializedName
import com.hellmund.primetime.database.HistoryMovie
import com.hellmund.primetime.utils.Constants
import java.util.*

data class Sample(
        val id: Int,
        val title: String,
        @SerializedName("poster_path") val poster: String,
        val popularity: Double,
        val releaseDate: Date,
        var selected: Boolean = false
) {

    fun toggleSelected() {
        selected = selected.not()
    }

    fun toHistoryMovie(): HistoryMovie {
        return HistoryMovie(id, title, Constants.LIKE, Date(), false)
    }

}
