package com.hellmund.primetime.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.data.repositories.WatchlistRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel @Inject constructor(
    repository: WatchlistRepository
) : ViewModel() {

    private val _watchlistCount = MutableLiveData<Int>()
    val watchlistCount: LiveData<Int> = _watchlistCount

    init {
        viewModelScope.launch {
            repository
                .observeAll()
                .map { it.size }
                .collect { _watchlistCount.value = it }
        }
    }
}
