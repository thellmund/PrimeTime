package com.hellmund.primetime.main

import com.hellmund.primetime.model.HistoryMovie
import com.hellmund.primetime.model.Movie
import java.util.*

interface MainContract {

    interface Presenter {
        fun getToolbarSubtitle(): String
        fun getRecommendations(): ArrayList<Movie>
        fun setRecommendations(recommendations: ArrayList<Movie>)
        fun getMovieAt(position: Int): Movie
        fun genreAlreadySelected(selected: String): Boolean
        fun handleGenreDialogInput(selected: String, which: Int)
        fun getHistory(): ArrayList<HistoryMovie>
        fun setupSingleMovieRecommendations(id: Int, title: String)
        fun setupCategoryRecommendations(category: String)
        fun addToWatchlist(position: Int)
        fun saveInWatchlistOnDevice(movie: Movie)
        fun addMovieRating(position: Int, rating: Int)
        fun removeFromWatchlist(id: Int)
        fun downloadRecommendationsAsync()
        fun showUndoToast(id: Int, rating: Int)
        fun onWatchlist(id: Int): Boolean
        fun detachView()
    }

}