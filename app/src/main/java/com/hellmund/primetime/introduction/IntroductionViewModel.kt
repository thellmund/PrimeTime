package com.hellmund.primetime.introduction

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.hellmund.primetime.main.RecommendationsRepository
import com.hellmund.primetime.utils.plusAssign
import io.reactivex.disposables.CompositeDisposable

class IntroductionViewModel(
        private val repository: RecommendationsRepository
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

}
