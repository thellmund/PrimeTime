package com.hellmund.primetime.data.model

import android.os.Parcelable
import com.hellmund.api.model.FullApiMovie
import com.hellmund.api.model.PartialApiMovie
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate

@Parcelize
data class PartialMovie(
    val id: Long,
    val posterPath: String?,
    val backdropPath: String?,
    val title: String,
    val genreIds: List<Long>,
    val description: String?,
    val releaseDate: LocalDate?,
    val popularity: Float,
    val voteAverage: Float,
    val voteCount: Int
) : Parcelable {

    companion object {

        fun from(apiMovie: PartialApiMovie) = PartialMovie(
            id = apiMovie.id,
            posterPath = apiMovie.posterPath,
            backdropPath = apiMovie.backdropPath,
            title = apiMovie.title,
            genreIds = apiMovie.genreIds,
            description = apiMovie.description,
            releaseDate = apiMovie.releaseDate,
            popularity = apiMovie.popularity,
            voteAverage = apiMovie.voteAverage,
            voteCount = apiMovie.voteCount
        )
    }
}

@Parcelize
data class MovieGenre(
    val id: Long,
    val name: String,
    val isPreferred: Boolean,
    val isExcluded: Boolean
) : Parcelable

@Parcelize
data class Movie(
    val id: Long,
    val posterPath: String,
    val backdropPath: String,
    val title: String,
    val genres: List<MovieGenre>,
    val description: String,
    val releaseDate: LocalDate,
    val popularity: Float,
    val voteAverage: Float,
    val voteCount: Int,
    val runtime: Int,
    val imdbId: String
) : Parcelable {

    enum class WatchStatus {
        NOT_WATCHED, ON_WATCHLIST, WATCHED
    }

    companion object {

        fun from(apiMovie: FullApiMovie): Movie? {
            val backdropPath = apiMovie.backdropPath ?: return null
            val posterPath = apiMovie.posterPath ?: return null

            return Movie(
                id = apiMovie.id,
                backdropPath = backdropPath,
                posterPath = posterPath,
                title = apiMovie.title,
                genres = apiMovie.genres.map {
                    MovieGenre(it.id, it.name, isPreferred = false, isExcluded = true)
                },
                description = apiMovie.description,
                releaseDate = apiMovie.releaseDate,
                popularity = apiMovie.popularity,
                voteAverage = apiMovie.voteAverage,
                voteCount = apiMovie.voteCount,
                runtime = apiMovie.runtime,
                imdbId = apiMovie.imdbId
            )
        }
    }
}
