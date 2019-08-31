package com.hellmund.primetime.history.ui

import com.hellmund.primetime.data.model.HistoryMovie
import com.hellmund.primetime.data.model.Rating

data class HistoryMovieViewEntity(
    val id: Long,
    val title: String,
    val rating: Rating,
    val formattedTimestamp: String,
    val detailsText: String,
    val raw: HistoryMovie
) {

    fun apply(rating: Rating) = RatedHistoryMovie(this, rating)

}

data class RatedHistoryMovie(val movie: HistoryMovieViewEntity, val rating: Rating)
