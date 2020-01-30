package com.hellmund.primetime.onboarding.ui.selectmovies

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.data.model.HistoryMovie
import com.hellmund.primetime.data.model.Rating
import com.hellmund.primetime.data.repositories.GenresRepository
import com.hellmund.primetime.onboarding.domain.Sample
import com.hellmund.primetime.onboarding.domain.SamplesRepository
import com.hellmund.primetime.ui_common.viewmodel.Reducer
import com.hellmund.primetime.ui_common.viewmodel.SingleEvent
import com.hellmund.primetime.ui_common.viewmodel.SingleEventStore
import com.hellmund.primetime.ui_common.viewmodel.ViewStateStore
import com.hellmund.primetime.ui_common.viewmodel.viewStateStore
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime.now
import java.io.IOException
import javax.inject.Inject

data class SelectMoviesViewState(
    val pages: Int = 1,
    val data: List<Sample> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: Throwable? = null
) {
    val isError: Boolean
        get() = error != null
}

sealed class ViewEvent {
    object Refresh : ViewEvent()
    data class ItemClicked(val sample: Sample) : ViewEvent()
    data class Store(val samples: List<Sample>) : ViewEvent()
}

sealed class ViewResult {
    object Loading : ViewResult()
    data class Data(val data: List<Sample>, val page: Int) : ViewResult()
    data class Error(val error: Throwable) : ViewResult()
    data class SelectionChanged(val sample: Sample) : ViewResult()
    object None : ViewResult()
}

sealed class NavigationResult {
    object OpenNext : NavigationResult()
}

class SamplesViewStateReducer : Reducer<SelectMoviesViewState, ViewResult> {
    override fun invoke(
        state: SelectMoviesViewState,
        viewResult: ViewResult
    ) = when (viewResult) {
        is ViewResult.Loading -> state.copy(isLoading = true, error = null)
        is ViewResult.Data -> {
            val data = if (viewResult.page == 1) viewResult.data else state.data + viewResult.data
            state.copy(pages = viewResult.page, data = data, isLoading = false, error = null)
        }
        is ViewResult.Error -> state.copy(isLoading = false, error = viewResult.error)
        is ViewResult.SelectionChanged -> {
            val items = state.data
            val index = items.indexOfFirst { it.id == viewResult.sample.id }
            val newItems = items.toMutableList()
            newItems[index] = viewResult.sample
            state.copy(data = newItems)
        }
        is ViewResult.None -> state
    }
}

class SelectMoviesViewModel @Inject constructor(
    private val repository: SamplesRepository,
    private val genresRepository: GenresRepository
) : ViewModel() {

    private val navigationResultsStore = SingleEventStore<NavigationResult>()
    val navigationResults: LiveData<SingleEvent<NavigationResult>> = navigationResultsStore.events

    private val store = viewStateStore(
        initialState = SelectMoviesViewState(),
        reducer = SamplesViewStateReducer()
    )

    val viewState: LiveData<SelectMoviesViewState> = store.viewState

    private var page: Int = 1

    init {
        viewModelScope.launch {
            store.dispatch(ViewResult.Loading)
            store.dispatch(fetchMovies(page))
        }
    }

    private suspend fun fetchMovies(
        page: Int
    ): ViewResult {
        return try {
            val genres = genresRepository.getPreferredGenres()
            val recommendations = repository.fetch(genres, page)
            ViewResult.Data(recommendations, page)
        } catch (e: IOException) {
            ViewResult.Error(e)
        }
    }

    private fun toggleSelection(sample: Sample) {
        val newSample = sample.copy(isSelected = sample.isSelected.not())
        store.dispatch(ViewResult.SelectionChanged(newSample))
    }

    private suspend fun storeSelection(samples: List<Sample>) {
        val historyMovies = samples.map {
            HistoryMovie.Impl(
                id = it.id,
                title = it.title,
                rating = Rating.Like,
                timestamp = now()
            )
        }
        repository.store(historyMovies)
        navigationResultsStore.dispatch(NavigationResult.OpenNext)
    }

    fun dispatch(viewEvent: ViewEvent) {
        viewModelScope.launch {
            when (viewEvent) {
                is ViewEvent.Refresh -> fetchMovies(page)
                is ViewEvent.ItemClicked -> toggleSelection(viewEvent.sample)
                is ViewEvent.Store -> storeSelection(viewEvent.samples)
            }
        }
    }
}
