package com.hellmund.primetime.ui.suggestions.data

import com.hellmund.primetime.data.api.ApiService
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.ui.history.HistoryRepository
import com.hellmund.primetime.ui.selectgenres.GenresRepository
import com.hellmund.primetime.ui.suggestions.MovieViewEntity
import com.hellmund.primetime.ui.suggestions.RecommendationsType
import com.hellmund.primetime.ui.suggestions.VideoResolver
import com.hellmund.primetime.ui.suggestions.details.Review
import com.hellmund.primetime.utils.ErrorHelper
import com.hellmund.primetime.utils.OnboardingHelper
import io.reactivex.Observable
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

data class MoviesResponse(val results: List<Movie>)

interface MoviesRepository {
    fun fetchRecommendations(type: RecommendationsType, page: Int): Observable<List<Movie>>
    fun fetchRecommendations(movieId: Int, page: Int = 1): Observable<List<Movie>>
    fun fetchVideo(movie: MovieViewEntity): Observable<String>
    fun fetchMovie(movieId: Int): Observable<Movie>
    fun searchMovies(query: String): Observable<List<Movie>>
    fun fetchPopularMovies(): Observable<List<Movie>>
    fun fetchReviews(movieId: Int): Observable<List<Review>>
}

class RealMoviesRepository @Inject constructor(
        private val apiService: ApiService,
        private val genresRepository: GenresRepository,
        private val historyRepository: HistoryRepository,
        private val onboardingHelper: OnboardingHelper
) : MoviesRepository {

    private val resultsZipper = ResultsZipper()

    override fun fetchRecommendations(
            type: RecommendationsType,
            page: Int
    ): Observable<List<Movie>> {
        return when (type) {
            is RecommendationsType.Personalized -> fetchPersonalizedRecommendations(type.genres, page)
            is RecommendationsType.BasedOnMovie -> fetchMovieBasedRecommendations(type.id, page)
            is RecommendationsType.NowPlaying -> fetchNowPlayingRecommendations(page)
            is RecommendationsType.Upcoming -> fetchUpcomingRecommendations(page)
            is RecommendationsType.ByGenre -> fetchGenreRecommendations(type.genre.id, page)
        }
    }

    private fun fetchPersonalizedRecommendations(
            filterGenres: List<Genre>? = null,
            page: Int
    ): Observable<List<Movie>> {
        // TODO: Fix this workaround
        // TODO: Add new movies to results
        if (onboardingHelper.isFirstLaunch) {
            return fetchTopRatedMovies(page)
        }

        val personalized = historyRepository
                .getLiked()
                .flattenAsObservable { it }
                .sorted()
                .take(10)
                .flatMap { fetchRecommendations(it.id, page) }
                .toList()
                .map { it.flatten() }
                .toObservable()

        val genres = filterGenres?.let { Observable.just(it) } ?: genresRepository.preferredGenres
        val byGenre = genres
                .flatMapIterable { it }
                .flatMap { fetchGenreRecommendations(it.id, page) }

        val topRated = fetchTopRatedMovies(page)

        return Observable.zip(personalized, byGenre, topRated, resultsZipper)
    }

    private fun fetchMovieBasedRecommendations(
            movieId: Int,
            page: Int
    ): Observable<List<Movie>> {
        return fetchRecommendations(movieId, page).subscribeOn(Schedulers.io())
    }

    private fun fetchNowPlayingRecommendations(
            page: Int
    ): Observable<List<Movie>> {
        return apiService
                .nowPlaying(page)
                .doOnError(ErrorHelper.logAndIgnore())
                .subscribeOn(Schedulers.io())
                .map { it.results }
    }

    private fun fetchUpcomingRecommendations(
            page: Int
    ): Observable<List<Movie>> {
        return apiService
                .upcoming(page)
                .doOnError(ErrorHelper.logAndIgnore())
                .subscribeOn(Schedulers.io())
                .map { it.results }
    }

    override fun fetchRecommendations(
            movieId: Int,
            page: Int
    ): Observable<List<Movie>> {
        return apiService
                .recommendations(movieId, page)
                .doOnError(ErrorHelper.logAndIgnore())
                .subscribeOn(Schedulers.io())
                .map { it.results }
    }

    private fun fetchGenreRecommendations(
            genreId: Int,
            page: Int = 1
    ): Observable<List<Movie>> {
        return apiService
                .genreRecommendations(genreId, page)
                .doOnError(ErrorHelper.logAndIgnore())
                .subscribeOn(Schedulers.io())
                .map {
                    it.results
                }
    }

    private fun fetchTopRatedMovies(
            page: Int = 1
    ): Observable<List<Movie>> {
        return apiService
                .topRatedMovies(page)
                .doOnError(ErrorHelper.logAndIgnore())
                .subscribeOn(Schedulers.io())
                .map { it.results }
    }

    override fun fetchVideo(movie: MovieViewEntity): Observable<String> {
        return apiService
                .videos(movie.id)
                .doOnError(ErrorHelper.logAndIgnore())
                .subscribeOn(Schedulers.io())
                .map { it.results }
                .map { VideoResolver.findBest(movie.title, it) }
    }

    override fun fetchMovie(movieId: Int): Observable<Movie> {
        return apiService
                .movie(movieId)
                .doOnError(ErrorHelper.logAndIgnore())
                .subscribeOn(Schedulers.io())
    }

    override fun searchMovies(query: String): Observable<List<Movie>> {
        return apiService
                .search(query)
                .doOnError(ErrorHelper.logAndIgnore())
                .subscribeOn(Schedulers.io())
                .map { it.results }
    }

    override fun fetchPopularMovies(): Observable<List<Movie>> {
        return apiService
                .popular()
                .doOnError(ErrorHelper.logAndIgnore())
                .subscribeOn(Schedulers.io())
                .map { it.results }
    }

    override fun fetchReviews(movieId: Int): Observable<List<Review>> {
        return apiService
                .reviews(movieId)
                .doOnError(ErrorHelper.logAndIgnore())
                .subscribeOn(Schedulers.io())
                .map { it.results }
    }

    class ResultsZipper : Function3<List<Movie>, List<Movie>, List<Movie>, List<Movie>> {

        override fun apply(t1: List<Movie>, t2: List<Movie>, t3: List<Movie>): List<Movie> {
            return t1 + t2 + t3
        }

    }

}
