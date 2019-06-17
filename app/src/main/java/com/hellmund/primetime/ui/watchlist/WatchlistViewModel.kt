package com.hellmund.primetime.ui.watchlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.ui.history.HistoryRepository
import com.hellmund.primetime.ui.shared.ViewStateStore
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WatchlistViewState(
        val data: List<WatchlistMovieViewEntity> = emptyList(),
        val isLoading: Boolean = false,
        val error: Throwable? = null,
        val deletedIndex: Int? = null
)

sealed class Result {
    data class Data(val data: List<WatchlistMovieViewEntity>) : Result()
    data class Error(val error: Throwable) : Result()
    data class Removed(val movie: WatchlistMovieViewEntity) : Result()
    data class NotificationToggled(val movie: WatchlistMovieViewEntity) : Result()
}

class WatchlistViewStateStore : ViewStateStore<WatchlistViewState, Result>(WatchlistViewState()) {

    override fun reduceState(
        state: WatchlistViewState,
        result: Result
    ): WatchlistViewState {
        return when (result) {
            is Result.Data -> state.copy(data = result.data, isLoading = false, error = null, deletedIndex = null)
            is Result.Error -> state.copy(isLoading = false, error = result.error, deletedIndex = null)
            is Result.Removed -> {
                val index = state.data.indexOf(result.movie)
                state.copy(data = state.data.minus(result.movie), deletedIndex = index)
            }
            is Result.NotificationToggled -> {
                val index = state.data.indexOfFirst { it.id == result.movie.id }
                val newData = state.data.toMutableList()
                newData[index] = result.movie
                state.copy(data = newData)
            }
        }
    }

}

class WatchlistViewModel @Inject constructor(
        private val repository: WatchlistRepository,
        private val historyRepository: HistoryRepository,
        viewEntityMapper: WatchlistMovieViewEntityMapper
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val store = WatchlistViewStateStore()
    val viewState: LiveData<WatchlistViewState> = store.viewState

    init {
        /*compositeDisposable += repository.getAllRx()
            .onErrorReturn { emptyList() }
            .map(viewEntityMapper)
            .subscribe {
                store.dispatch(Result.Data(it))
            }*/

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
        repository.remove(movie.id)
        val historyMovie = HistoryMovie.fromWatchlistMovie(movie, rating)
        historyRepository.store(historyMovie)
        store.dispatch(Result.Removed(movie))
    }

    fun remove(movie: WatchlistMovieViewEntity) {
        viewModelScope.launch {
            removeMovie(movie)
        }
    }

    fun toggleNotification(movie: WatchlistMovieViewEntity) {
        viewModelScope.launch {
            toggleAndStoreNotification(movie)
        }
    }

    fun onMovieRated(movie: WatchlistMovieViewEntity, rating: Int) {
        viewModelScope.launch {
            rateMovie(movie, rating)
        }
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

}
