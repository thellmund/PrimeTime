package com.hellmund.primetime.ui.history

data class HistoryMovieViewEntity(
        val id: Int,
        val title: String,
        val rating: Int,
        val formattedTimestamp: String,
        val detailsText: String
)
