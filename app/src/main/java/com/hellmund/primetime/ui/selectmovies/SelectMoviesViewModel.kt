package com.hellmund.primetime.ui.selectmovies

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hellmund.primetime.ui.selectgenres.GenresRepository
import com.hellmund.primetime.utils.plusAssign
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
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

sealed class Action {
    data class Refresh(val page: Int = 1) : Action()
    data class Selected(val sample: Sample) : Action()
}

sealed class Result {
    object Loading : Result()
    data class Data(val data: List<Sample>, val page: Int) : Result()
    data class Error(val error: Throwable) : Result()
    data class SelectionChanged(val sample: Sample) : Result()
    object None : Result()
}

class SelectMoviesViewModel @Inject constructor(
        private val repository: SamplesRepository,
        private val genresRepository: GenresRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val refreshRelay = PublishRelay.create<Action>()

    private val _viewState = MutableLiveData<SelectMoviesViewState>()
    val viewState: LiveData<SelectMoviesViewState> = _viewState

    private var page: Int = 1

    init {
        val initialViewState = SelectMoviesViewState(isLoading = true)
        compositeDisposable += refreshRelay
                .switchMap(this::processAction)
                .scan(initialViewState, this::reduceState)
                .subscribe(this::render)
        refreshRelay.accept(Action.Refresh())
    }

    private fun processAction(action: Action): Observable<Result> {
        return when (action) {
            is Action.Refresh -> fetchMovies(action.page)
            is Action.Selected -> toggleSelection(action.sample)
        }
    }

    private fun fetchMovies(
            page: Int
    ): Observable<Result> {
        return genresRepository.preferredGenres
                .flatMap { repository.fetch(it, page) }
                .subscribeOn(Schedulers.io())
                .doOnNext { this.page++ }
                .map { Result.Data(it, page) as Result }
                .onErrorReturn { Result.Error(it) }
                .startWith(if (page == 1) Result.Loading else Result.None)
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
            is Result.Data -> {
                val data = if (result.page == 1) result.data else viewState.data + result.data
                viewState.copy(pages = result.page, data = data, isLoading = false, error = null)
            }
            is Result.Error -> viewState.copy(isLoading = false, error = result.error)
            is Result.SelectionChanged -> {
                // TODO Clean up
                val items = viewState.data
                val index = items.indexOfFirst { it.id == result.sample.id }
                val newItems = items.toMutableList()
                newItems[index] = result.sample
                viewState.copy(data = newItems)
            }
            is Result.None -> viewState
        }
    }

    private fun render(viewState: SelectMoviesViewState) {
        _viewState.postValue(viewState)
    }

    fun refresh() {
        refreshRelay.accept(Action.Refresh(page))
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
            private val repository: SamplesRepository,
            private val genresRepository: GenresRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SelectMoviesViewModel(repository, genresRepository) as T
        }

    }

}
