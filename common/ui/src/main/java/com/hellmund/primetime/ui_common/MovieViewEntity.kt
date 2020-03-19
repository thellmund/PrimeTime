package com.hellmund.primetime.ui_common

import android.os.Parcelable
import com.hellmund.primetime.data.model.HistoryMovie
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.data.model.PartialMovie
import com.hellmund.primetime.data.model.Rating
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDateTime

sealed class MovieViewEntity {
    abstract val id: Long
    abstract val posterUrl: String
    abstract val backdropUrl: String
    abstract val title: String
    abstract val formattedGenres: String
    abstract val description: String
    abstract val releaseYear: String
    abstract val popularity: Float
    abstract val formattedVoteAverage: String
    abstract val formattedVoteCount: String
    abstract val formattedRuntime: String
    abstract val imdbId: String?

    @Parcelize
    data class Partial(
        override val id: Long,
        override val posterUrl: String,
        override val backdropUrl: String,
        override val title: String,
        override val formattedGenres: String,
        override val description: String,
        override val releaseYear: String,
        override val popularity: Float,
        override val formattedVoteAverage: String,
        override val formattedVoteCount: String,
        override val formattedRuntime: String,
        override val imdbId: String? = null,
        val raw: PartialMovie
    ) : MovieViewEntity(), Parcelable {
        operator fun plus(rating: Rating) = RatedMovie.Partial(this, rating)
    }

    @Parcelize
    data class Full(
        override val id: Long,
        override val posterUrl: String,
        override val backdropUrl: String,
        override val title: String,
        override val formattedGenres: String,
        override val description: String,
        override val releaseYear: String,
        override val popularity: Float,
        override val formattedVoteAverage: String,
        override val formattedVoteCount: String,
        override val formattedRuntime: String,
        override val imdbId: String? = null,
        val raw: Movie
    ) : MovieViewEntity(), Parcelable {
        operator fun plus(rating: Rating) = RatedMovie.Full(this, rating)
    }
}

sealed class RatedMovie {
    abstract val rating: Rating

    data class Partial(
        val movie: MovieViewEntity.Partial,
        override val rating: Rating
    ) : RatedMovie() {
        fun toHistoryMovie() = HistoryMovie.Impl(
            id = movie.id,
            title = movie.title,
            rating = rating,
            timestamp = LocalDateTime.now()
        )
    }

    data class Full(
        val movie: MovieViewEntity.Full,
        override val rating: Rating
    ) : RatedMovie()
}
