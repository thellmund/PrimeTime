package com.hellmund.primetime.data.database

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.hellmund.primetime.data.model.SearchResult
import com.hellmund.primetime.ui.main.Rating
import org.threeten.bp.LocalDate

@Entity(tableName = "history_movies")
data class HistoryMovie(
        @PrimaryKey var id: Int,
        var title: String,
        var rating: Int,
        var timestamp: LocalDate,
        var isUpdating: Boolean
): Comparable<HistoryMovie> {

    override fun compareTo(other: HistoryMovie): Int = other.timestamp.compareTo(timestamp)

    companion object {

        fun fromSearchResult(searchResult: SearchResult, rating: Int): HistoryMovie {
            return HistoryMovie(searchResult.id, searchResult.title, rating, LocalDate.now(), false)
        }

        fun fromRating(rating: Rating): HistoryMovie {
            val movie = rating.movie
            val ratingValue = if (rating is Rating.Like) 1 else 0
            return HistoryMovie(movie.id, movie.title, ratingValue, LocalDate.now(), false)
        }

    }

}
