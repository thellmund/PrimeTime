package com.hellmund.primetime.ui.history

import com.hellmund.primetime.data.database.HistoryMovie

data class HistoryMovieViewEntity(
    val id: Int,
    val title: String,
    val rating: Int,
    val formattedTimestamp: String,
    val detailsText: String,
    val raw: HistoryMovie
)
