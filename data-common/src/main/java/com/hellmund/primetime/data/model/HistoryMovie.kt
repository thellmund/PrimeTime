package com.hellmund.primetime.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDateTime

@Entity(tableName = "history_movies")
data class HistoryMovie(
    @PrimaryKey var id: Int,
    var title: String,
    var rating: Rating,
    var timestamp: LocalDateTime
)
