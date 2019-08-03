package com.hellmund.primetime.ui.watchlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.ui.history.HistoryRepository
import com.hellmund.primetime.ui.shared.Reducer
import com.hellmund.primetime.ui.shared.ViewStateStore
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class Action {
    data class Remove(val item: WatchlistMovieViewEntity) : Action()
    data class ToggleNotification(val item: WatchlistMovieViewEntity) : Action()
    data class RateMovie(val item: WatchlistMovieViewEntity, val rating: Int) : Action()
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

class WatchlistViewModel @Inject constructor(
    private val repository: WatchlistRepository,
    private val historyRepository: HistoryRepository,
    viewEntityMapper: WatchlistMovieViewEntityMapper
) : ViewModel() {

    private val store = WatchlistViewStateStore()
    val viewState: LiveData<WatchlistViewState> = store.viewState

    init {
        viewModelScope.launch {
            val movies = repository.getAll()
            val viewEntities = viewEntityMapper.apply(movies)
            store.dispatch(Result.Data(viewEntities))
        }
    }

    private suspend fun toggleAndStoreNotification(movie: WatchlistMovieViewEntity) {
        val newMovie = movie.raw.copy(notificationsActivated = movie.raw.notificationsActivated.not())
        val newViewEntity = movie.copy(notificationsActivated = movie.notificationsActivated.not())
        repository.store(newMovie)
        store.dispatch(Result.NotificationToggled(newViewEntity))
    }

    private suspend fun removeMovie(movie: WatchlistMovieViewEntity) {
        repository.remove(movie.id)
        store.dispatch(Result.Removed(movie))
    }

    private suspend fun rateMovie(movie: WatchlistMovieViewEntity, rating: Int) {
        val historyMovie = HistoryMovie.fromWatchlistMovie(movie, rating)
        repository.remove(movie.id)
        historyRepository.store(historyMovie)
        store.dispatch(Result.Removed(movie))
    }

    fun dispatch(action: Action) {
        viewModelScope.launch {
            when (action) {
                is Action.Remove -> removeMovie(action.item)
                is Action.ToggleNotification -> toggleAndStoreNotification(action.item)
                is Action.RateMovie -> rateMovie(action.item, action.rating)
            }
        }
    }

}
