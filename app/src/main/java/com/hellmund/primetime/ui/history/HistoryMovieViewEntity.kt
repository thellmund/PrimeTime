package com.hellmund.primetime.ui.history

import com.hellmund.primetime.data.model.HistoryMovie
import com.hellmund.primetime.data.model.Rating

data class HistoryMovieViewEntity(
    val id: Int,
    val title: String,
    val rating: Rating,
    val formattedTimestamp: String,
    val detailsText: String,
    val raw: HistoryMovie
) {

    fun apply(rating: Rating) = RatedHistoryMovie(this, rating)

}

data class RatedHistoryMovie(val movie: HistoryMovieViewEntity, val rating: Rating)
