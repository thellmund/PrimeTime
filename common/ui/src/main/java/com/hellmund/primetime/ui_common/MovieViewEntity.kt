package com.hellmund.primetime.ui_common

import android.os.Parcelable
import com.hellmund.primetime.data.model.HistoryMovie
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.data.model.Rating
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDateTime

@Parcelize
data class MovieViewEntity(
    val id: Long,
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
) : Parcelable {

    fun apply(rating: Rating) = RatedMovie(this, rating)

}

data class RatedMovie(val movie: MovieViewEntity, val rating: Rating) {
    fun toHistoryMovie() = HistoryMovie.Impl(
        id = movie.id,
        title = movie.title,
        rating = rating,
        timestamp = LocalDateTime.now()
    )
}