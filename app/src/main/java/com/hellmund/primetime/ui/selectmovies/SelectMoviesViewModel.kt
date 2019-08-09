package com.hellmund.primetime.ui.selectmovies

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellmund.api.Sample
import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.ui.selectgenres.GenresRepository
import com.hellmund.primetime.ui.shared.Reducer
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

class SamplesViewStateReducer : Reducer<SelectMoviesViewState, Result> {
    override fun invoke(
        state: SelectMoviesViewState,
        result: Result
    ) = when (result) {
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

class SamplesViewStateStore : ViewStateStore<SelectMoviesViewState, Result>(
    initialState = SelectMoviesViewState(),
    reducer = SamplesViewStateReducer()
)

sealed class Action {
    object Refresh : Action()
    data class ItemClicked(val sample: Sample) : Action()
    data class Store(val samples: List<Sample>) : Action()
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

    private val store = SamplesViewStateStore()
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
        val historyMovies = samples.map { HistoryMovie.from(it) }
        repository.store(historyMovies)
        store.dispatch(Result.Finished)
    }

    fun dispatch(action: Action) {
        viewModelScope.launch {
            when (action) {
                is Action.Refresh -> fetchMovies(page)
                is Action.ItemClicked -> toggleSelection(action.sample)
                is Action.Store -> storeSelection(action.samples)
            }
        }
    }

}
