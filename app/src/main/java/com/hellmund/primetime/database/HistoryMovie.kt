package com.hellmund.primetime.database

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.content.Context
import com.hellmund.primetime.R
import com.hellmund.primetime.main.Rating
import com.hellmund.primetime.model.SearchResult
import com.hellmund.primetime.utils.Constants
import com.hellmund.primetime.utils.DateUtils
import java.util.*

@Entity(tableName = "history_movies")
data class HistoryMovie(
        @PrimaryKey var id: Int,
        var title: String,
        var rating: Int,
        var timestamp: Date,
        var isUpdating: Boolean
) {

    fun getDetailsText(context: Context): String {
        val rating = getPrettyRating(context)
        val timestamp = getPrettyTimestamp()
        return context.getString(R.string.added_on, rating, timestamp)
    }

    private fun getPrettyRating(context: Context): String {
        val resId = if (rating == Constants.LIKE) R.string.liked else R.string.disliked
        return context.getString(resId)
    }

    private fun getPrettyTimestamp(): String? {
        return DateUtils.getDateInLocalFormat(timestamp)

    }

    companion object {

        fun fromSearchResult(searchResult: SearchResult, rating: Int): HistoryMovie {
            return HistoryMovie(searchResult.id, searchResult.title, rating, Date(), false)
        }

        fun fromRating(rating: Rating): HistoryMovie {
            val movie = rating.movie
            val ratingValue = if (rating is Rating.Like) 1 else 0
            return HistoryMovie(movie.id, movie.title, ratingValue, Date(), false)
        }

    }

}
