package com.hellmund.primetime.ui.watchlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.data.HistoryRepository
import com.hellmund.primetime.ui_common.Reducer
import com.hellmund.primetime.ui_common.ViewStateStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class Action {
    data class Remove(val item: WatchlistMovieViewEntity) : Action()
    data class ToggleNotification(val item: WatchlistMovieViewEntity) : Action()
    data class RateMovie(val item: RatedWatchlistMovie) : Action()
}

sealed class Result {
    data class Data(val data: List<WatchlistMovieViewEntity>) : Result()
    data class Error(val error: Throwable) : Result()
    data class Removed(val movie: WatchlistMovieViewEntity) : Result()
    data class NotificationToggled(val movie: WatchlistMovieViewEntity) : Result()
}

class WatchlistViewStateReducer : Reducer<WatchlistViewState, Result> {

    override fun invoke(
        state: WatchlistViewState,
        result: Result
    ): WatchlistViewState = when (result) {
        is Result.Data -> state.toData(result.data)
        is Result.Error -> state.toError(result.error)
        is Result.Removed -> state.remove(result.movie)
        is Result.NotificationToggled -> {
            val index = state.data.indexOfFirst { it.id == result.movie.id }
            val newData = state.data.toMutableList()
            newData[index] = result.movie
            state.copy(data = newData)
        }
    }

}

class WatchlistViewStateStore : ViewStateStore<WatchlistViewState, Result>(
    initialState = WatchlistViewState(),
    reducer = WatchlistViewStateReducer()
)

@ExperimentalCoroutinesApi
@FlowPreview
class WatchlistViewModel @Inject constructor(
    private val repository: WatchlistRepository,
    private val historyRepository: HistoryRepository,
    viewEntityMapper: WatchlistMovieViewEntityMapper
) : ViewModel() {

    private val store = WatchlistViewStateStore()
    val viewState: LiveData<WatchlistViewState> = store.viewState

    init {
        viewModelScope.launch {
            repository
                .observeAll()
                .map { viewEntityMapper(it) }
                .filter { true }
                .collect { store.dispatch(Result.Data(it)) }
        }
    }

    private suspend fun toggleAndStoreNotification(movie: WatchlistMovieViewEntity) {
        // TODO Move this out of WatchlistMovie model
        val newMovie = movie.raw.copy(notificationsActivated = movie.raw.notificationsActivated.not())
        repository.store(newMovie)
        val newViewEntity = movie.copy(notificationsActivated = movie.notificationsActivated.not())
        store.dispatch(Result.NotificationToggled(newViewEntity))
    }

    private suspend fun removeMovie(movie: WatchlistMovieViewEntity) {
        repository.remove(movie.id)
        store.dispatch(Result.Removed(movie))
    }

    private suspend fun rateMovie(ratedMovie: RatedWatchlistMovie) {
        val historyMovie = ratedMovie.toHistoryMovie()
        repository.remove(ratedMovie.movie.id)
        historyRepository.store(historyMovie)
        store.dispatch(Result.Removed(ratedMovie.movie))
    }

    fun dispatch(action: Action) {
        viewModelScope.launch {
            when (action) {
                is Action.Remove -> removeMovie(action.item)
                is Action.ToggleNotification -> toggleAndStoreNotification(action.item)
                is Action.RateMovie -> rateMovie(action.item)
            }
        }
    }

}
