package com.hellmund.primetime.search.ui

import android.os.Parcelable
import com.hellmund.primetime.data.model.HistoryMovie
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.data.model.Rating
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDateTime

@Parcelize
data class SearchViewEntity(
    val id: Int,
    val posterUrl: String,
    val backdropUrl: String,
    val title: String,
    val formattedGenres: String,
    val description: String,
    val releaseYear: String,
    val popularity: Float,
    val formattedVoteAverage: String,
    val formattedVoteCount: String,
    val formattedRuntime: String,
    val imdbId: String? = null,
    val raw: Movie
) : Parcelable

data class RatedMovie(val movie: SearchViewEntity, val rating: Rating) {
    fun toHistoryMovie() = HistoryMovie(movie.id, movie.title, rating, LocalDateTime.now())
}
