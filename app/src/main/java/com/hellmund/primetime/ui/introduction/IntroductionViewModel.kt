package com.hellmund.primetime.ui.introduction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.ui.recommendations.MoviesRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class IntroductionViewModel @Inject constructor(
    private val repository: MoviesRepository
) : ViewModel() {

    private val _posterUrls = MutableLiveData<List<String>>()
    val posterUrls: LiveData<List<String>> = _posterUrls

    init {
        viewModelScope.launch {
            _posterUrls.value = repository.fetchPopularMovies().map { it.fullPosterUrl }
        }
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
