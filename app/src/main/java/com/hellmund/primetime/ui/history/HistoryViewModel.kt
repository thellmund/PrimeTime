package com.hellmund.primetime.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.ui.shared.ViewStateStore
import com.hellmund.primetime.utils.plusAssign
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryViewState(
        val data: List<HistoryMovieViewEntity> = emptyList(),
        val isLoading: Boolean = false,
        val error: Throwable? = null
)

sealed class Result {
    data class Data(val data: List<HistoryMovieViewEntity>) : Result()
    data class Error(val error: Throwable) : Result()
    data class Removed(val movie: HistoryMovieViewEntity) : Result()
    data class Updated(val movie: HistoryMovieViewEntity) : Result()
}

class HistoryViewStateStore(
    initialState: HistoryViewState
) : ViewStateStore<HistoryViewState, Result>(initialState) {

    override fun reduceState(
        state: HistoryViewState,
        result: Result
    ): HistoryViewState {
        return when (result) {
            is Result.Data -> state.copy(data = result.data, isLoading = false, error = null)
            is Result.Error -> state.copy(isLoading = false, error = result.error)
            is Result.Removed -> state.copy(data = state.data.minus(result.movie))
            is Result.Updated -> {
                val index = state.data.indexOfFirst { it.id == result.movie.id }
                val newData = state.data
                    .toMutableList()
                    .apply { set(index, result.movie) }
                    .toList()
                state.copy(data = newData)
            }
        }
    }

}

@FlowPreview
class HistoryViewModel @Inject constructor(
        private val repository: HistoryRepository,
        private val viewEntitiesMapper: HistoryMoviesViewEntityMapper,
        private val viewEntityMapper: HistoryMovieViewEntityMapper
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val store = HistoryViewStateStore(HistoryViewState())
    val viewState: LiveData<HistoryViewState> = store.viewState

    init {
        compositeDisposable += repository.getAll()
            .map(viewEntitiesMapper)
            .map { Result.Data(it) as Result }
            .onErrorReturn { Result.Error(it) }
            .subscribe { store.dispatch(it) }
    }

    private fun removeMovie(movie: HistoryMovieViewEntity) {
        viewModelScope.launch {
            repository.remove(movie.id)
            store.dispatch(Result.Removed(movie))
        }
    }

    private fun updateMovie(movie: HistoryMovie) {
        viewModelScope.launch {
            repository.store(movie)
            val viewEntity = viewEntityMapper.apply(movie)
            store.dispatch(Result.Updated(viewEntity))
        }
    }

    fun update(movie: HistoryMovieViewEntity, newRating: Int) {
        val newMovie = movie.raw.copy(rating = newRating)
        updateMovie(newMovie)
    }

    fun remove(movie: HistoryMovieViewEntity) {
        removeMovie(movie)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

}
