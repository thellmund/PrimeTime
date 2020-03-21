package com.hellmund.primetime.history.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.data.repositories.HistoryRepository
import com.hellmund.primetime.ui_common.viewmodel.Reducer
import com.hellmund.primetime.ui_common.viewmodel.viewStateStore
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class HistoryViewState(
    val data: List<HistoryMovieViewEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: Throwable? = null,
    val movieToBeRated: HistoryMovieViewEntity? = null,
    val movieToBeEdited: HistoryMovieViewEntity? = null
)

sealed class ViewEvent {
    data class Update(val ratedMovie: RatedHistoryMovie) : ViewEvent()
    data class Remove(val movie: HistoryMovieViewEntity) : ViewEvent()
}

sealed class ViewResult {
    data class Data(val data: List<HistoryMovieViewEntity>) : ViewResult()
    data class Error(val error: Throwable) : ViewResult()
    data class Removed(val movie: HistoryMovieViewEntity) : ViewResult()
}

class HistoryViewStateReducer : Reducer<HistoryViewState, ViewResult> {
    override fun invoke(
        state: HistoryViewState,
        viewResult: ViewResult
    ) = when (viewResult) {
        is ViewResult.Data -> state.copy(data = viewResult.data, isLoading = false, error = null)
        is ViewResult.Error -> state.copy(isLoading = false, error = viewResult.error)
        is ViewResult.Removed -> state.copy(data = state.data.minus(viewResult.movie))
    }
}

@ExperimentalCoroutinesApi
@FlowPreview
class HistoryViewModel @Inject constructor(
    private val repository: HistoryRepository,
    private val viewEntitiesMapper: HistoryMovieViewEntitiesMapper
) : ViewModel() {

    private val store = viewStateStore(
        initialState = HistoryViewState(),
        reducer = HistoryViewStateReducer()
    )

    val viewState: LiveData<HistoryViewState> = store.viewState

    init {
        viewModelScope.launch {
            repository.observeAll()
                .map { it.sortedByDescending { movie -> movie.timestamp } }
                .map { viewEntitiesMapper(it) }
                .map { ViewResult.Data(it) as ViewResult }
                .catch { emit(ViewResult.Error(it)) }
                .collect { store.dispatch(it) }
        }
    }

    private suspend fun removeMovie(movie: HistoryMovieViewEntity) {
        repository.remove(movie.id)
        store.dispatch(ViewResult.Removed(movie))
    }

    private suspend fun updateMovie(ratedMovie: RatedHistoryMovie) {
        repository.updateRating(ratedMovie.movie.raw, ratedMovie.rating)
    }

    fun handleViewEvent(viewEvent: ViewEvent) {
        viewModelScope.launch {
            when (viewEvent) {
                is ViewEvent.Update -> updateMovie(viewEvent.ratedMovie)
                is ViewEvent.Remove -> removeMovie(viewEvent.movie)
            }
        }
    }
}
