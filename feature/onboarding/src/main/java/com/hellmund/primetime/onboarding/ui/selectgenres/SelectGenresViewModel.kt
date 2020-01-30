package com.hellmund.primetime.onboarding.ui.selectgenres

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.repositories.GenresRepository
import com.hellmund.primetime.ui_common.viewmodel.Reducer
import com.hellmund.primetime.ui_common.viewmodel.SingleEvent
import com.hellmund.primetime.ui_common.viewmodel.SingleEventStore
import com.hellmund.primetime.ui_common.viewmodel.ViewStateStore
import com.hellmund.primetime.ui_common.viewmodel.viewStateStore
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

data class SelectGenresViewState(
    val data: List<Genre> = emptyList(),
    val isLoading: Boolean = false,
    val error: Throwable? = null
)

sealed class ViewEvent {
    data class ToggleGenre(val genre: Genre) : ViewEvent()
    data class Store(val genres: List<Genre>) : ViewEvent()
}

sealed class ViewResult {
    object Loading : ViewResult()
    data class Data(val data: List<Genre>) : ViewResult()
    data class Error(val error: Throwable) : ViewResult()
    data class GenreToggled(val genre: Genre) : ViewResult()
    object None : ViewResult()
}

sealed class NavigationResult {
    object OpenNext : NavigationResult()
}

class GenresViewStateReducer : Reducer<SelectGenresViewState, ViewResult> {
    override fun invoke(
        state: SelectGenresViewState,
        viewResult: ViewResult
    ) = when (viewResult) {
        is ViewResult.Loading -> state.copy(isLoading = true, error = null)
        is ViewResult.Data -> state.copy(data = viewResult.data, isLoading = false, error = null)
        is ViewResult.Error -> state.copy(isLoading = false, error = viewResult.error)
        is ViewResult.GenreToggled -> {
            val index = state.data.indexOfFirst { it.id == viewResult.genre.id }
            val newData = state.data.replace(index, viewResult.genre)
            state.copy(data = newData)
        }
        is ViewResult.None -> state
    }
}

class SelectGenresViewModel @Inject constructor(
    private val repository: GenresRepository
) : ViewModel() {

    private val store = viewStateStore(
        initialState = SelectGenresViewState(),
        reducer = GenresViewStateReducer()
    )

    val viewState: LiveData<SelectGenresViewState> = store.viewState

    private val navigationResultsStore = SingleEventStore<NavigationResult>()
    val navigationResults: LiveData<SingleEvent<NavigationResult>> = navigationResultsStore.events

    init {
        viewModelScope.launch {
            store.dispatch(ViewResult.Loading)
            store.dispatch(fetchMovies())
        }
    }

    private suspend fun fetchMovies(): ViewResult {
        return try {
            val genres = repository.fetchGenres()
            ViewResult.Data(genres)
        } catch (e: IOException) {
            ViewResult.Error(e)
        }
    }

    private fun toggleGenre(genre: Genre) {
        val newGenre = Genre.Impl(
            id = genre.id,
            name = genre.name,
            isPreferred = !genre.isPreferred,
            isExcluded = genre.isExcluded
        )
        store.dispatch(ViewResult.GenreToggled(newGenre))
    }

    private suspend fun storeGenres(genres: List<Genre>) {
        repository.storeGenres(genres)
        navigationResultsStore.dispatch(NavigationResult.OpenNext)
    }

    fun dispatch(viewEvent: ViewEvent) {
        viewModelScope.launch {
            when (viewEvent) {
                is ViewEvent.ToggleGenre -> toggleGenre(viewEvent.genre)
                is ViewEvent.Store -> storeGenres(viewEvent.genres)
            }
        }
    }
}

private fun <T> List<T>.replace(index: Int, element: T): List<T> {
    return toMutableList().apply {
        removeAt(index)
        add(index, element)
    }
}
