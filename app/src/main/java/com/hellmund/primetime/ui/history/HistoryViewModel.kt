package com.hellmund.primetime.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.domain.ObserveHistoryUseCase
import com.hellmund.primetime.ui.shared.Reducer
import com.hellmund.primetime.ui.shared.ViewStateStore
import com.hellmund.primetime.utils.replace
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MIN_HISTORY_SIZE = 4

data class HistoryViewState(
    val data: List<HistoryMovieViewEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: Throwable? = null,
    val showRemoveFailedWarning: Boolean = false
)

sealed class Action {
    data class Update(val ratedMovie: RatedHistoryMovie) : Action()
    data class Remove(val movie: HistoryMovieViewEntity) : Action()
}

sealed class Result {
    data class Data(val data: List<HistoryMovieViewEntity>) : Result()
    data class Error(val error: Throwable) : Result()
    data class Removed(val movie: HistoryMovieViewEntity) : Result()
    data class Updated(val movie: HistoryMovieViewEntity) : Result()
    object ShowRemoveFailedWarning : Result()
    object HideRemoveFailedWarning : Result()
}

class HistoryViewStateReducer : Reducer<HistoryViewState, Result> {
    override fun invoke(
        state: HistoryViewState,
        result: Result
    ) = when (result) {
        is Result.Data -> state.copy(data = result.data, isLoading = false, error = null)
        is Result.Error -> state.copy(isLoading = false, error = result.error)
        is Result.Removed -> state.copy(data = state.data.minus(result.movie))
        is Result.Updated -> {
            val index = state.data.indexOfFirst { it.id == result.movie.id }
            val newData = state.data.replace(index, result.movie)
            state.copy(data = newData)
        }
        is Result.ShowRemoveFailedWarning -> state.copy(showRemoveFailedWarning = true)
        is Result.HideRemoveFailedWarning -> state.copy(showRemoveFailedWarning = false)
    }
}

class HistoryViewStateStore : ViewStateStore<HistoryViewState, Result>(
    initialState = HistoryViewState(),
    reducer = HistoryViewStateReducer()
)

@ExperimentalCoroutinesApi
@FlowPreview
class HistoryViewModel @Inject constructor(
    private val observeHistory: ObserveHistoryUseCase,
    private val repository: HistoryRepository,
    private val viewEntityMapper: HistoryMovieViewEntityMapper
) : ViewModel() {

    private val store = HistoryViewStateStore()
    val viewState: LiveData<HistoryViewState> = store.viewState

    init {
        viewModelScope.launch {
            observeHistory()
                .map { Result.Data(it) as Result }
                .catch { emit(Result.Error(it)) }
                .collect { value -> store.dispatch(value) }
        }
    }

    private suspend fun removeMovie(movie: HistoryMovieViewEntity) {
        val canRemove = store.state().data.size > MIN_HISTORY_SIZE
        if (canRemove) {
            repository.remove(movie.id)
            store.dispatch(Result.Removed(movie))
        } else {
            store.dispatch(Result.ShowRemoveFailedWarning)
            store.dispatch(Result.HideRemoveFailedWarning)
        }
    }

    private suspend fun updateMovie(ratedMovie: RatedHistoryMovie) {
        val newMovie = ratedMovie.movie.raw.copy(rating = ratedMovie.rating)
        repository.store(newMovie)

        val viewEntity = viewEntityMapper(newMovie)
        store.dispatch(Result.Updated(viewEntity))
    }

    fun dispatch(action: Action) {
        viewModelScope.launch {
            when (action) {
                is Action.Update -> updateMovie(action.ratedMovie)
                is Action.Remove -> removeMovie(action.movie)
            }
        }
    }

}
