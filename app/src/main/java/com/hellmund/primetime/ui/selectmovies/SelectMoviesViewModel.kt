package com.hellmund.primetime.ui.selectmovies

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.ui.selectgenres.GenresRepository
import com.hellmund.primetime.ui.shared.ViewStateStore
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

data class SelectMoviesViewState(
        val pages: Int = 1,
        val data: List<Sample> = emptyList(),
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
        val error: Throwable? = null,
        val isFinished: Boolean = false
) {
    val isError: Boolean
        get() = error != null
}

class SamplesViewStateStore(
    initialState: SelectMoviesViewState
) : ViewStateStore<SelectMoviesViewState, Result>(initialState) {

    override fun reduceState(
        state: SelectMoviesViewState,
        result: Result
    ): SelectMoviesViewState {
        return when (result) {
            is Result.Loading -> state.copy(isLoading = true, error = null)
            is Result.Data -> {
                val data = if (result.page == 1) result.data else state.data + result.data
                state.copy(pages = result.page, data = data, isLoading = false, error = null)
            }
            is Result.Error -> state.copy(isLoading = false, error = result.error)
            is Result.SelectionChanged -> {
                val items = state.data
                val index = items.indexOfFirst { it.id == result.sample.id }
                val newItems = items.toMutableList()
                newItems[index] = result.sample
                state.copy(data = newItems)
            }
            is Result.Finished -> state.copy(isFinished = true)
            is Result.None -> state
        }
    }

}

sealed class Result {
    object Loading : Result()
    data class Data(val data: List<Sample>, val page: Int) : Result()
    data class Error(val error: Throwable) : Result()
    data class SelectionChanged(val sample: Sample) : Result()
    object Finished : Result()
    object None : Result()
}

class SelectMoviesViewModel @Inject constructor(
        private val repository: SamplesRepository,
        private val genresRepository: GenresRepository
) : ViewModel() {

    private val store = SamplesViewStateStore(SelectMoviesViewState())
    val viewState: LiveData<SelectMoviesViewState> = store.viewState

    private var page: Int = 1

    init {
        viewModelScope.launch {
            store.dispatch(Result.Loading)
            store.dispatch(fetchMovies(page))
        }
    }

    private suspend fun fetchMovies(
            page: Int
    ): Result {
        return try {
            val genres = genresRepository.getPreferredGenres()
            val recommendations = repository.fetch(genres, page)
            Result.Data(recommendations, page)
        } catch (e: IOException) {
            Result.Error(e)
        }
    }

    private fun toggleSelection(sample: Sample) {
        val newSample = sample.copy(selected = sample.selected.not())
        store.dispatch(Result.SelectionChanged(newSample))
    }

    private suspend fun storeSelection(samples: List<Sample>) {
        val historyMovies = samples.map { it.toHistoryMovie() }
        repository.store(historyMovies)
        store.dispatch(Result.Finished)
    }

    fun refresh() {
        viewModelScope.launch {
            fetchMovies(page)
        }
    }

    fun onItemClick(sample: Sample) {
        toggleSelection(sample)
    }

    fun store(movies: List<Sample>) {
        viewModelScope.launch {
            storeSelection(movies)
        }
    }

    class Factory(
            private val repository: SamplesRepository,
            private val genresRepository: GenresRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SelectMoviesViewModel(repository, genresRepository) as T
        }

    }

}
