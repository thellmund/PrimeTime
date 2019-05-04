package com.hellmund.primetime.ui.suggestions.data

import android.util.Log
import com.hellmund.primetime.data.api.ApiService
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.ui.history.HistoryRepository
import com.hellmund.primetime.ui.selectgenres.GenresRepository
import com.hellmund.primetime.ui.suggestions.MovieViewEntity
import com.hellmund.primetime.ui.suggestions.RecommendationsType
import com.hellmund.primetime.ui.suggestions.VideoResolver
import io.reactivex.Observable
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class MoviesRepository @Inject constructor(
        private val apiService: ApiService,
        private val genresRepository: GenresRepository,
        private val historyRepository: HistoryRepository
) {

    private val resultsZipper = ResultsZipper()

    fun fetchRecommendations(type: RecommendationsType): Observable<List<Movie>> {
        return when (type) {
            is RecommendationsType.Personalized -> fetchPersonalizedRecommendations()
            is RecommendationsType.BasedOnMovie -> fetchMovieBasedRecommendations(type.id)
            is RecommendationsType.NowPlaying -> fetchNowPlayingRecommendations()
            is RecommendationsType.Upcoming -> fetchUpcomingRecommendations()
            is RecommendationsType.ByGenre -> fetchGenreRecommendations(type.genre.id)
        }
    }

    private fun fetchPersonalizedRecommendations(): Observable<List<Movie>> {
        val personalized = historyRepository
                .getAll()
                .flattenAsObservable { it }
                .sorted()
                .take(10)
                .flatMap { fetchRecommendations(it.id) }
                .toList()
                .map { it.flatten() }
                .toObservable()

        val genres = genresRepository
                .preferredGenres
                .flatMapIterable { it }
                .flatMap { fetchGenreRecommendations(it.id) }

        val topRated = fetchTopRatedMovies()

        return Observable.zip(personalized, genres, topRated, resultsZipper)
    }

    private fun fetchMovieBasedRecommendations(movieId: Int): Observable<List<Movie>> {
        return fetchRecommendations(movieId).subscribeOn(Schedulers.io())
    }

    private fun fetchNowPlayingRecommendations(): Observable<List<Movie>> {
        return apiService
                .nowPlaying()
                .subscribeOn(Schedulers.io())
                .map { it.results }
    }

    private fun fetchUpcomingRecommendations(): Observable<List<Movie>> {
        return apiService
                .upcoming()
                .subscribeOn(Schedulers.io())
                .map { it.results }
    }

    private fun fetchRecommendations(movieId: Int): Observable<List<Movie>> {
        return apiService
                .recommendations(movieId)
                .subscribeOn(Schedulers.io())
                .map { it.results }
    }

    private fun fetchGenreRecommendations(genreId: Int): Observable<List<Movie>> {
        return apiService
                .genreRecommendations(genreId)
                .subscribeOn(Schedulers.io())
                .map { it.results }
    }

    private fun fetchTopRatedMovies(): Observable<List<Movie>> {
        return apiService
                .topRatedMovies()
                .subscribeOn(Schedulers.io())
                .map { it.results }
    }

    fun fetchVideo(movie: MovieViewEntity): Observable<String> {
        return apiService
                .videos(movie.id)
                .subscribeOn(Schedulers.io())
                .map { it.results }
                .map { VideoResolver.findBest(movie.title, it) }
    }

    fun fetchMovie(movieId: Int): Observable<Movie> {
        return apiService
                .movie(movieId)
                .subscribeOn(Schedulers.io())
    }

    fun searchMovies(query: String): Observable<List<Movie>> {
        return apiService
                .search(query)
                .subscribeOn(Schedulers.io())
                .doOnError { Log.d("TAG", "", it) }
                .map { it.results }
    }

    fun fetchPopularMovies(): Observable<List<Movie>> {
        return apiService
                .popular()
                .subscribeOn(Schedulers.io())
                .map { it.results }
    }

    class ResultsZipper : Function3<List<Movie>, List<Movie>, List<Movie>, List<Movie>> {

        override fun apply(t1: List<Movie>, t2: List<Movie>, t3: List<Movie>): List<Movie> {
            return t1 + t2 + t3
        }

    }

}
