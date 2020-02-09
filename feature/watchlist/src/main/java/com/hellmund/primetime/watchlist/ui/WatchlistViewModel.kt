package com.hellmund.primetime.watchlist.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellmund.primetime.core.OnboardingHelper
import com.hellmund.primetime.core.notifications.NotificationUtils
import com.hellmund.primetime.data.repositories.HistoryRepository
import com.hellmund.primetime.data.repositories.WatchlistRepository
import com.hellmund.primetime.ui_common.viewmodel.Reducer
import com.hellmund.primetime.ui_common.viewmodel.viewStateStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ViewEvent {
    data class Remove(val item: WatchlistMovieViewEntity) : ViewEvent()
    data class ToggleNotification(val item: WatchlistMovieViewEntity) : ViewEvent()
    data class RateMovie(val item: RatedWatchlistMovie) : ViewEvent()
}

sealed class ViewResult {
    data class Data(val data: List<WatchlistMovieViewEntity>) : ViewResult()
    data class Error(val error: Throwable) : ViewResult()
    data class Removed(val movie: WatchlistMovieViewEntity) : ViewResult()
    data class NotificationToggled(val movie: WatchlistMovieViewEntity) : ViewResult()
    data class HistoryButtonToggled(val isVisible: Boolean) : ViewResult()
}

class WatchlistViewStateReducer : Reducer<WatchlistViewState, ViewResult> {

    override fun invoke(
        state: WatchlistViewState,
        viewResult: ViewResult
    ): WatchlistViewState = when (viewResult) {
        is ViewResult.Data -> state.toData(viewResult.data)
        is ViewResult.Error -> state.toError(viewResult.error)
        is ViewResult.Removed -> state.remove(viewResult.movie)
        is ViewResult.NotificationToggled -> {
            val index = state.data.indexOfFirst { it.id == viewResult.movie.id }
            val newData = state.data.toMutableList()
            newData[index] = viewResult.movie
            state.copy(data = newData)
        }
        is ViewResult.HistoryButtonToggled -> state.copy(showHistoryButton = viewResult.isVisible)
    }
}

@ExperimentalCoroutinesApi
@FlowPreview
class WatchlistViewModel @Inject constructor(
    private val repository: WatchlistRepository,
    private val historyRepository: HistoryRepository,
    private val notificationUtils: NotificationUtils,
    viewEntityMapper: WatchlistMovieViewEntityMapper,
    onboardingHelper: OnboardingHelper
) : ViewModel() {

    private val store = viewStateStore(
        initialState = WatchlistViewState(),
        reducer = WatchlistViewStateReducer()
    )

    val viewState: LiveData<WatchlistViewState> = store.viewState

    init {
        viewModelScope.launch {
            historyRepository
                .observeAll()
                .map { it.isNotEmpty() }
                .collect { store.dispatch(ViewResult.HistoryButtonToggled(isVisible = it)) }
        }

        viewModelScope.launch {
            repository
                .observeAll()
                .map { viewEntityMapper(it) }
                .filter { true }
                .collect { store.dispatch(ViewResult.Data(it)) }
        }
    }

    private fun toggleAndStoreNotification(
        movie: WatchlistMovieViewEntity
    ) = viewModelScope.launch {
        repository.toggleNotification(movie.raw)
        val newViewEntity = movie.copy(notificationsActivated = movie.notificationsActivated.not())
        store.dispatch(ViewResult.NotificationToggled(newViewEntity))

        if (newViewEntity.notificationsActivated) {
            notificationUtils.scheduleNotification(movie.raw)
        } else {
            notificationUtils.cancelNotification(movie.raw)
        }
    }

    private fun removeMovie(movie: WatchlistMovieViewEntity) = viewModelScope.launch {
        repository.remove(movie.id)
        store.dispatch(ViewResult.Removed(movie))
    }

    private fun rateMovie(ratedMovie: RatedWatchlistMovie) = viewModelScope.launch {
        val historyMovie = ratedMovie.toHistoryMovie()
        repository.remove(ratedMovie.movie.id)
        historyRepository.store(historyMovie)
        store.dispatch(ViewResult.Removed(ratedMovie.movie))
    }

    fun dispatch(viewEvent: ViewEvent) {
        when (viewEvent) {
            is ViewEvent.Remove -> removeMovie(viewEvent.item)
            is ViewEvent.ToggleNotification -> toggleAndStoreNotification(viewEvent.item)
            is ViewEvent.RateMovie -> rateMovie(viewEvent.item)
        }
    }
}
