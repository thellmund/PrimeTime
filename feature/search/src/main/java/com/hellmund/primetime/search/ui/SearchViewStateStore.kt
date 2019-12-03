package com.hellmund.primetime.search.ui

import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.ui_common.PartialMovieViewEntity
import com.hellmund.primetime.ui_common.viewmodel.Reducer
import com.hellmund.primetime.ui_common.viewmodel.ViewStateStore

data class SearchViewState(
    val genres: List<Genre> = emptyList(),
    val data: List<PartialMovieViewEntity> = emptyList(),
    val showClearButton: Boolean = false,
    val didPerformSearch: Boolean = false,
    val isLoading: Boolean = false,
    val error: Throwable? = null,
    val snackbarTextResId: Int? = null
) {

    val showPlaceholder: Boolean
        get() = data.isEmpty() && didPerformSearch
}

class SearchViewStateReducer : Reducer<SearchViewState, ViewResult> {
    override fun invoke(
        state: SearchViewState,
        viewResult: ViewResult
    ) = when (viewResult) {
        is ViewResult.GenresLoaded -> state.copy(genres = viewResult.genres)
        is ViewResult.Loading -> state.copy(isLoading = true, error = null)
        is ViewResult.Data -> state.copy(data = viewResult.data, isLoading = false, error = null, didPerformSearch = true)
        is ViewResult.Error -> state.copy(isLoading = false, error = viewResult.error, didPerformSearch = true)
        is ViewResult.ToggleClearButton -> state.copy(showClearButton = viewResult.show)
        is ViewResult.ShowSnackbar -> state.copy(snackbarTextResId = viewResult.messageResId)
        is ViewResult.DismissSnackbar -> state.copy(snackbarTextResId = null)
    }
}

class SearchViewStateStore : ViewStateStore<SearchViewState, ViewResult>(
    initialState = SearchViewState(),
    reducer = SearchViewStateReducer()
)
