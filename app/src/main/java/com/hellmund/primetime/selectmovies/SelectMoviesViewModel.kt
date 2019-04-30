package com.hellmund.primetime.selectmovies

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.hellmund.primetime.model.Sample
import com.hellmund.primetime.selectgenres.GenresRepository
import com.hellmund.primetime.utils.plusAssign
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import javax.inject.Inject

data class SelectMoviesViewState(
        val data: List<Sample> = emptyList(),
        val isLoading: Boolean = false,
        val error: Throwable? = null
) {
    val isError: Boolean
        get() = error != null
}

sealed class Action {
    object Refresh : Action()
    data class Selected(val sample: Sample) : Action()
}

sealed class Result {
    object Loading : Result()
    data class Data(val data: List<Sample>) : Result()
    data class Error(val error: Throwable) : Result()
    data class SelectionChanged(val sample: Sample) : Result()
}

class SelectMoviesViewModel @Inject constructor(
        private val repository: SelectMoviesRepository,
        private val genresRepository: GenresRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val refreshRelay = PublishRelay.create<Action>()

    private val _viewState = MutableLiveData<SelectMoviesViewState>()
    val viewState: LiveData<SelectMoviesViewState> = _viewState

    init {
        val initialViewState = SelectMoviesViewState(isLoading = true)
        compositeDisposable += refreshRelay
                .switchMap(this::processAction)
                .scan(initialViewState, this::reduceState)
                .subscribe(this::render)
        refreshRelay.accept(Action.Refresh)
    }

    private fun processAction(action: Action): Observable<Result> {
        return when (action) {
            is Action.Refresh -> fetchMovies()
            is Action.Selected -> toggleSelection(action.sample)
        }
    }

    private fun fetchMovies(): Observable<Result> {
        return genresRepository.preferredGenres
                .flatMap { repository.fetch(it) }
                .subscribeOn(Schedulers.io())
                .map { Result.Data(it) as Result }
                .onErrorReturn { Result.Error(it) }
                .startWith(Result.Loading)
    }

    private fun toggleSelection(sample: Sample): Observable<Result> {
        val newSample = sample.copy(selected = sample.selected.not())
        return Observable.just(Result.SelectionChanged(newSample))
    }

    private fun reduceState(
            viewState: SelectMoviesViewState,
            result: Result
    ): SelectMoviesViewState {
        return when (result) {
            is Result.Loading -> viewState.copy(isLoading = true, error = null)
            is Result.Data -> viewState.copy(data = result.data, isLoading = false, error = null)
            is Result.Error -> viewState.copy(isLoading = false, error = result.error)
            is Result.SelectionChanged -> {
                // TODO Clean up
                val items = viewState.data
                val index = items.indexOfFirst { it.id == result.sample.id }
                val newItems = items.toMutableList()
                newItems[index] = result.sample
                viewState.copy(data = newItems)
            }
        }
    }

    private fun render(viewState: SelectMoviesViewState) {
        _viewState.postValue(viewState)
    }

    fun refresh() {
        refreshRelay.accept(Action.Refresh)
    }

    fun onItemClick(sample: Sample) {
        refreshRelay.accept(Action.Selected(sample))
    }

    fun store(movies: List<Sample>) {
        doAsync {
            val historyMovies = movies.map { it.toHistoryMovie() }
            repository.store(historyMovies)
        }
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    class Factory(
            private val repository: SelectMoviesRepository,
            private val genresRepository: GenresRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SelectMoviesViewModel(repository, genresRepository) as T
        }

    }

}
