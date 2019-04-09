package com.hellmund.primetime.main

import com.hellmund.primetime.api.ApiService
import com.hellmund.primetime.model.SearchResult
import com.hellmund.primetime.model2.ApiMovie
import com.hellmund.primetime.utils.GenresProvider
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class RecommendationsRepository(
        private val apiService: ApiService,
        private val genresProvider: GenresProvider
) {

    fun fetchRecommendations(type: RecommendationsType): Observable<List<ApiMovie>> {
        return when (type) {
            is RecommendationsType.Personalized -> fetchPersonalizedRecommendations()
            is RecommendationsType.BasedOnMovie -> fetchMovieBasedRecommendations(type.id)
            is RecommendationsType.NowPlaying -> fetchNowPlayingRecommendations()
            is RecommendationsType.Upcoming -> fetchUpcomingRecommendations()
            is RecommendationsType.ByGenre -> fetchGenreRecommendations(type.genre.id)
        }
    }

    private fun fetchPersonalizedRecommendations(): Observable<List<ApiMovie>> {
        return Observable.fromCallable {
            val history = emptyList<ApiMovie>()
            val results = mutableListOf<ApiMovie>()

            for (movie in history) {
                results += fetchRecommendations(movie.id).blockingFirst()
            }

            val genres = genresProvider.getPreferredGenres()
            for (genre in genres) {
                results += fetchGenreRecommendations(genre.toInt()).blockingFirst()
            }

            results += fetchTopRatedMovies().blockingFirst()

            // TODO: Filter results

            results
        }
    }

    private fun fetchMovieBasedRecommendations(movieId: Int): Observable<List<ApiMovie>> {
        return fetchRecommendations(movieId).subscribeOn(Schedulers.io())
    }

    private fun fetchNowPlayingRecommendations(): Observable<List<ApiMovie>> {
        return apiService
                .nowPlaying()
                .subscribeOn(Schedulers.io())
                .map { it.results }
    }

    private fun fetchUpcomingRecommendations(): Observable<List<ApiMovie>> {
        return apiService
                .upcoming()
                .subscribeOn(Schedulers.io())
                .map { it.results }
    }

    private fun fetchRecommendations(movieId: Int): Observable<List<ApiMovie>> {
        return apiService
                .recommendations(movieId)
                .subscribeOn(Schedulers.io())
                .map { it.results }
    }

    private fun fetchGenreRecommendations(genreId: Int): Observable<List<ApiMovie>> {
        return apiService
                .genreRecommendations(genreId)
                .subscribeOn(Schedulers.io())
                .map { it.results }
    }

    private fun fetchTopRatedMovies(): Observable<List<ApiMovie>> {
        return apiService
                .topRatedMovies()
                .subscribeOn(Schedulers.io())
                .map { it.results }
    }

    // TODO Move somewhere else
    fun fetchVideo(movie: ApiMovie): Observable<String> {
        return apiService
                .videos(movie.id)
                .subscribeOn(Schedulers.io())
                .map { it.results }
                .map { VideoResolver.findBest(movie.title, it) }
    }

    fun fetchMovie(movieId: Int): Observable<ApiMovie> {
        return apiService
                .movie(movieId)
                .subscribeOn(Schedulers.io())
    }

    fun searchMovies(query: String): Observable<List<SearchResult>> {
        return apiService
                .search(query)
                .subscribeOn(Schedulers.io())
                .map {
                    movies -> movies.results.map(SearchResult::fromMovie)
                }
    }

}
