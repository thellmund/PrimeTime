package com.hellmund.primetime.search.ui

import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.ui_common.MovieViewEntity
import com.hellmund.primetime.ui_common.viewmodel.Reducer
import com.hellmund.primetime.ui_common.viewmodel.ViewStateStore

data class SearchViewState(
    val genres: List<Genre> = emptyList(),
    val data: List<MovieViewEntity> = emptyList(),
    val showClearButton: Boolean = false,
    val didPerformSearch: Boolean = false,
    val isLoading: Boolean = false,
    val error: Throwable? = null,
    val snackbarTextResId: Int? = null
) {

    val showPlaceholder: Boolean
        get() = data.isEmpty() && didPerformSearch
}

class SearchViewStateReducer : Reducer<SearchViewState, Result> {
    override fun invoke(
        state: SearchViewState,
        result: Result
    ) = when (result) {
        is Result.GenresLoaded -> state.copy(genres = result.genres)
        is Result.Loading -> state.copy(isLoading = true, error = null)
        is Result.Data -> state.copy(data = result.data, isLoading = false, error = null, didPerformSearch = true)
        is Result.Error -> state.copy(isLoading = false, error = result.error, didPerformSearch = true)
        is Result.ToggleClearButton -> state.copy(showClearButton = result.show)
        is Result.ShowSnackbar -> state.copy(snackbarTextResId = result.messageResId)
        is Result.DismissSnackbar -> state.copy(snackbarTextResId = null)
    }
}

class SearchViewStateStore : ViewStateStore<SearchViewState, Result>(
    initialState = SearchViewState(),
    reducer = SearchViewStateReducer()
)
