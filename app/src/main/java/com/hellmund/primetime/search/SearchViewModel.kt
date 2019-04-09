package com.hellmund.primetime.search

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.hellmund.primetime.main.RecommendationsRepository
import com.hellmund.primetime.model.SearchResult
import com.hellmund.primetime.utils.plusAssign
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

data class SearchViewState(
        val data: List<SearchResult> = emptyList(),
        val showClearButton: Boolean = false,
        val didPerformSearch: Boolean = false,
        val isLoading: Boolean = false,
        val error: Throwable? = null
) {

    val showPlaceholder: Boolean
        get() = data.isEmpty() && didPerformSearch

}

sealed class Action {
    data class Typed(val input: String) : Action()
    data class Search(val query: String) : Action()
}

sealed class Result {
    object Loading : Result()
    data class Data(val data: List<SearchResult>) : Result()
    data class Error(val error: Throwable) : Result()
    data class ToggleClearButton(val show: Boolean) : Result()
}

class SearchViewModel(
        private val repository: RecommendationsRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val actionsRelay = PublishRelay.create<Action>()

    private val _viewState = MutableLiveData<SearchViewState>()
    val viewState: LiveData<SearchViewState> = _viewState

    init {
        val initialViewState = SearchViewState()
        compositeDisposable += actionsRelay
                .switchMap(this::processAction)
                .scan(initialViewState, this::reduceState)
                .subscribe(this::render)
    }

    private fun processAction(action: Action): Observable<Result> {
        return when (action) {
            is Action.Typed -> onTyped(action.input)
            is Action.Search -> searchMovies(action.query)
        }
    }

    private fun onTyped(input: String): Observable<Result> {
        return Observable.just(Result.ToggleClearButton(input.isNotEmpty()))
    }

    private fun searchMovies(query: String): Observable<Result> {
        return repository.searchMovies(query)
                .map { Result.Data(it) as Result }
                .onErrorReturn { Result.Error(it) }
    }

    private fun reduceState(
            viewState: SearchViewState,
            result: Result
    ): SearchViewState {
        return when (result) {
            is Result.Loading -> viewState.copy(isLoading = true, error = null)
            is Result.Data -> viewState.copy(data = result.data, isLoading = false, error = null, didPerformSearch = true)
            is Result.Error -> viewState.copy(isLoading = false, error = result.error, didPerformSearch = true)
            is Result.ToggleClearButton -> viewState.copy(showClearButton = result.show)
        }
    }

    private fun render(viewState: SearchViewState) {
        _viewState.postValue(viewState)
    }

    fun search(query: String) {
        actionsRelay.accept(Action.Search(query))
    }

    fun onTextChanged(input: String) {
        actionsRelay.accept(Action.Typed(input))
    }

    fun addToWatchlist(searchResult: SearchResult) {
        TODO()
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    class Factory(
            private val repository: RecommendationsRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SearchViewModel(repository) as T
        }

    }

}
