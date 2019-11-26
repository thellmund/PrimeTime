package com.hellmund.primetime.data.model

import android.os.Parcelable
import com.hellmund.api.model.FullApiMovie
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import org.threeten.bp.LocalDate

@Parcelize
data class Movie(
    val id: Long,
    val posterPath: String,
    val backdropPath: String,
    val title: String,
    val genres: @RawValue List<Genre>,
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
                    Genre.Impl(it.id, it.name, isPreferred = false, isExcluded = true)
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
