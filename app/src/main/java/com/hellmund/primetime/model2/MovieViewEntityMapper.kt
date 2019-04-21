package com.hellmund.primetime.model2

import android.content.Context
import com.hellmund.primetime.R
import com.hellmund.primetime.selectgenres.GenresRepository
import com.hellmund.primetime.utils.DateUtils.getDateInLocalFormat
import io.reactivex.functions.Function
import java.util.*

class MovieViewEntityMapper(
        private val context: Context,
        private val genresRepo: GenresRepository
) : Function<List<ApiMovie>, List<MovieViewEntity>> {

    override fun apply(movies: List<ApiMovie>): List<MovieViewEntity> {
        return movies.map(this::createViewEntity)
    }

    private fun createViewEntity(movie: ApiMovie): MovieViewEntity {
        return MovieViewEntity(
                id = movie.id,
                posterUrl = "http://image.tmdb.org/t/p/w500${movie.posterPath}",
                title = movie.title,
                formattedGenres = getFormattedGenres(movie),
                description = movie.description,
                releaseYear = getReleaseYear(movie),
                popularity = movie.popularity,
                formattedVoteAverage = "${movie.voteAverage} / 10",
                runtime = movie.runtime,
                imdbId = movie.imdbId,
                raw = movie
        )
    }

    private fun getFormattedGenres(movie: ApiMovie): String {
        val genres = genresRepo.all.blockingGet().filter { movie.genreIds.contains(it.id) }
        return genres.map { it.name }.sorted().joinToString(", ")
    }

    private fun getReleaseYear(movie: ApiMovie): String {
        if (movie.releaseDate == null) {
            return context.getString(R.string.no_information)
        }

        val release = Calendar.getInstance()
        release.time = movie.releaseDate

        val now = Calendar.getInstance()

        return if (release.after(now)) {
            getDateInLocalFormat(release)
        } else {
            release.get(Calendar.YEAR).toString()
        }
    }

}
