package com.hellmund.primetime.main

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.hellmund.primetime.model2.ApiMovie
import com.hellmund.primetime.utils.plusAssign
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

data class MainViewState(
        val recommendationsType: RecommendationsType = RecommendationsType.Personalized,
        val data: List<ApiMovie> = emptyList(),
        val isLoading: Boolean = false,
        val error: Throwable? = null
) {

    val isError: Boolean
        get() = error != null

}

sealed class Action {
    data class RefreshMovieRecommendations(
            val type: RecommendationsType = RecommendationsType.Personalized
    ) : Action()
}

sealed class Result {
    object Loading : Result()
    data class Data(val data: List<ApiMovie>) : Result()
    data class Error(val error: Throwable) : Result()
}

class MainViewModel(
        private val repository: MoviesRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val refreshRelay = PublishRelay.create<Action>()

    private val _viewState = MutableLiveData<MainViewState>()
    val viewState: LiveData<MainViewState> = _viewState

    init {
        val initialViewState = MainViewState(isLoading = true)
        compositeDisposable += refreshRelay
                .switchMap(this::processAction)
                .scan(initialViewState, this::reduceState)
                .subscribe(this::render)
    }

    private fun processAction(action: Action): Observable<Result> {
        return when (action) {
            is Action.RefreshMovieRecommendations -> fetchRecommendations(action.type)
        }
    }

    private fun fetchRecommendations(type: RecommendationsType): Observable<Result> {
        return repository.fetchRecommendations(type)
                .subscribeOn(Schedulers.io())
                .map { Result.Data(it) as Result }
                .onErrorReturn { Result.Error(it) }
                .startWith(Result.Loading)
    }

    private fun reduceState(
            viewState: MainViewState,
            result: Result
    ): MainViewState {
        return when (result) {
            Result.Loading -> viewState.copy(isLoading = true, error = null)
            is Result.Data -> viewState.copy(data = result.data, isLoading = false, error = null)
            is Result.Error -> viewState.copy(isLoading = false, error = result.error)
        }
    }

    private fun render(viewState: MainViewState) {
        _viewState.postValue(viewState)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    fun refresh(recommendationsType: RecommendationsType) {
        refreshRelay.accept(Action.RefreshMovieRecommendations(recommendationsType))
    }

    class Factory(
            private val repository: MoviesRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainViewModel(repository) as T
        }

    }

}
