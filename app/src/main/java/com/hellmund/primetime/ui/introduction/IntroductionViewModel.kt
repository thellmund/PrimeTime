package com.hellmund.primetime.ui.introduction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hellmund.primetime.ui.suggestions.data.MoviesRepository
import com.hellmund.primetime.utils.plusAssign
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class IntroductionViewModel @Inject constructor(
    private val repository: MoviesRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _posterUrls = MutableLiveData<List<String>>()
    val posterUrls: LiveData<List<String>> = _posterUrls

    init {
        loadPosters()
    }

    private fun loadPosters() {
        compositeDisposable += repository
            .fetchPopularMovies()
            .map { movies -> movies.map { it.fullPosterUrl } }
            .subscribe(_posterUrls::postValue)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    class Factory(
        private val repository: MoviesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return IntroductionViewModel(repository) as T
        }
    }

}
