package com.hellmund.primetime.onboarding.selectgenres.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.data.repositories.GenresRepository
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.ui_common.Reducer
import com.hellmund.primetime.ui_common.SingleLiveDataEvent
import com.hellmund.primetime.ui_common.ViewStateStore
import com.hellmund.primetime.ui_common.replace
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

data class SelectGenresViewState(
    val data: List<Genre> = emptyList(),
    val isLoading: Boolean = false,
    val error: Throwable? = null
)

sealed class Action {
    data class ToggleGenre(val genre: Genre) : Action()
    data class Store(val genres: List<Genre>) : Action()
}

sealed class Result {
    object Loading : Result()
    data class Data(val data: List<Genre>) : Result()
    data class Error(val error: Throwable) : Result()
    data class GenreToggled(val genre: Genre) : Result()
    object None : Result()
}

class GenresViewStateReducer : Reducer<SelectGenresViewState, Result> {
    override fun invoke(
        state: SelectGenresViewState,
        result: Result
    ) = when (result) {
        is Result.Loading -> state.copy(isLoading = true, error = null)
        is Result.Data -> state.copy(data = result.data, isLoading = false, error = null)
        is Result.Error -> state.copy(isLoading = false, error = result.error)
        is Result.GenreToggled -> {
            val index = state.data.indexOfFirst { it.id == result.genre.id }
            val newData = state.data.replace(index, result.genre)
            state.copy(data = newData)
        }
        is Result.None -> state
    }
}

class NavigationEventStore<Event> {
    val event = MutableLiveData<Event>()
    fun dispatch(event: Event) { this.event.value = event }
}

class GenresViewStateStore : ViewStateStore<SelectGenresViewState, Result>(
    initialState = SelectGenresViewState(),
    reducer = GenresViewStateReducer()
)

class SelectGenresViewModel @Inject constructor(
    private val repository: GenresRepository
) : ViewModel() {

    private val store = GenresViewStateStore()
    private val navigationStore = NavigationEventStore<SingleLiveDataEvent<Unit>>()

    val viewState: LiveData<SelectGenresViewState> = store.viewState
    val navigation: LiveData<SingleLiveDataEvent<Unit>> = navigationStore.event

    init {
        viewModelScope.launch {
            store.dispatch(Result.Loading)
            store.dispatch(fetchMovies())
        }
    }

    private suspend fun fetchMovies(): Result {
        return try {
            val genres = repository.fetchGenres()
            Result.Data(genres)
        } catch (e: IOException) {
            Result.Error(e)
        }
    }

    private fun toggleGenre(genre: Genre) {
        val newGenre = genre.copy(isPreferred = !genre.isPreferred)
        store.dispatch(Result.GenreToggled(newGenre))
    }

    private suspend fun storeGenres(genres: List<Genre>) {
        repository.storeGenres(genres)
        navigationStore.dispatch(SingleLiveDataEvent(Unit))
    }

    fun dispatch(action: Action) {
        viewModelScope.launch {
            when (action) {
                is Action.ToggleGenre -> toggleGenre(action.genre)
                is Action.Store -> storeGenres(action.genres)
            }
        }
    }

}
