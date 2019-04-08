package com.hellmund.primetime.database

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.hellmund.primetime.main.Rating
import java.util.*

@Entity(tableName = "history_movies")
data class HistoryMovie(
        @PrimaryKey var id: Int,
        var title: String,
        var rating: Int,
        var timestamp: Date,
        var isUpdating: Boolean
) {

    companion object {

        fun fromRating(rating: Rating): HistoryMovie {
            val movie = rating.movie
            val ratingValue = if (rating is Rating.Like) 1 else 0
            return HistoryMovie(movie.id, movie.title, ratingValue, Date(), false)
        }

    }

}
