package com.hellmund.primetime.ui.selectgenres

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.ui.shared.Reducer
import com.hellmund.primetime.ui.shared.ViewStateStore
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

data class SelectGenresViewState(
        val data: List<Genre> = emptyList(),
        val isLoading: Boolean = false,
        val error: Throwable? = null,
        val isFinished: Boolean = false
)

sealed class Result {
    object Loading : Result()
    data class Data(val data: List<Genre>) : Result()
    data class Error(val error: Throwable) : Result()
    object Finish : Result()
}

class GenresViewStateReducer : Reducer<SelectGenresViewState, Result> {
    override fun invoke(
        state: SelectGenresViewState,
        result: Result
    ) = when (result) {
        is Result.Loading -> state.copy(isLoading = true, error = null)
        is Result.Data -> state.copy(data = result.data, isLoading = false, error = null)
        is Result.Error -> state.copy(isLoading = false, error = result.error)
        is Result.Finish -> state.copy(isFinished = true)
    }
}

class GenresViewStateStore : ViewStateStore<SelectGenresViewState, Result>(
    initialState = SelectGenresViewState(),
    reducer = GenresViewStateReducer()
)

class SelectGenresViewModel @Inject constructor(
        private val repository: GenresRepository
) : ViewModel() {

    private val store = GenresViewStateStore()

    val viewState: LiveData<SelectGenresViewState> = store.viewState

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

    private suspend fun storeGenres(genres: List<Genre>) {
        repository.storeGenres(genres)
        store.dispatch(Result.Finish)
    }

    fun store(genres: List<Genre>) {
        viewModelScope.launch {
            storeGenres(genres)
        }
    }

}
