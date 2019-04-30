package com.hellmund.primetime.model

import android.content.Context
import com.hellmund.primetime.R
import com.hellmund.primetime.selectgenres.GenresRepository
import com.hellmund.primetime.utils.DateUtils.getDateInLocalFormat
import io.reactivex.functions.Function
import java.util.*
import javax.inject.Inject

class MoviesViewEntityMapper @Inject constructor(
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
                formattedRuntime = getPrettyRuntime(movie),
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

    fun getPrettyRuntime(movie: ApiMovie): String {
        val runtime = movie.runtime ?: return context.getString(R.string.no_information)
        val hours = String.format(Locale.getDefault(), "%01d", runtime / 60)
        val minutes = String.format(Locale.getDefault(), "%02d", runtime % 60)
        return String.format("%s:%s", hours, minutes)
    }

}

class MovieViewEntityMapper @Inject constructor(
        private val context: Context,
        private val genresRepo: GenresRepository
) : Function<ApiMovie, MovieViewEntity> {

    override fun apply(movie: ApiMovie): MovieViewEntity {
        return MovieViewEntity(
                id = movie.id,
                posterUrl = "http://image.tmdb.org/t/p/w500${movie.posterPath}",
                title = movie.title,
                formattedGenres = getFormattedGenres(movie),
                description = movie.description,
                releaseYear = getReleaseYear(movie),
                popularity = movie.popularity,
                formattedVoteAverage = "${movie.voteAverage} / 10",
                formattedRuntime = getPrettyRuntime(movie),
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

    fun getPrettyRuntime(movie: ApiMovie): String {
        val runtime = movie.runtime ?: return context.getString(R.string.no_information)
        val hours = String.format(Locale.getDefault(), "%01d", runtime / 60)
        val minutes = String.format(Locale.getDefault(), "%02d", runtime % 60)
        return String.format("%s:%s", hours, minutes)
    }

}
