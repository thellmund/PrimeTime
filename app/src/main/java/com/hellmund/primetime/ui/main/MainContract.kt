package com.hellmund.primetime.ui.main

import com.hellmund.primetime.model.HistoryMovie
import com.hellmund.primetime.model.Movie
import java.util.*

interface MainContract {

    interface View {
        fun onDownloadStart()
        fun onSuccess()
        fun onError()
        fun onEmpty()
        fun onMovieRatingAdded(id: Int, rating: Int)
        fun tryDownloadAgain()
        fun openGenresDialog()
        fun openSearch()
        fun openWatchlist()
    }

    interface Presenter {
        fun attachView(view: View)
        fun loadIndices()
        fun handleShortcutOpen(intent: String)
        fun saveIndices()
        fun getToolbarSubtitle(): String
        fun getRecommendations(): ArrayList<Movie>
        fun setRecommendations(recommendations: ArrayList<Movie>)
        fun getMovieAt(position: Int): Movie
        fun genreAlreadySelected(selected: String): Boolean
        fun handleGenreDialogInput(selected: String, which: Int)
        fun getHistory(): ArrayList<HistoryMovie>
        fun setupSingleMovieRecommendations(id: Int, title: String)
        fun addToWatchlist(position: Int)
        fun saveInWatchlistOnDevice(movie: Movie)
        fun addMovieRating(position: Int, rating: Int)
        fun removeFromWatchlist(id: Int)
        fun forceRecommendationsDownload()
        fun downloadHistoryAndRecommendations()
        fun downloadRecommendationsAsync()
        fun showUndoToast(id: Int, rating: Int)
        fun onWatchlist(id: Int): Boolean
        fun detachView()
    }

}