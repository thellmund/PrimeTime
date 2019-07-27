package com.hellmund.primetime.ui.suggestions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.ui.history.HistoryRepository
import com.hellmund.primetime.ui.suggestions.data.MovieRankingProcessor
import com.hellmund.primetime.ui.suggestions.data.MoviesRepository
import com.hellmund.primetime.ui.suggestions.details.Rating
import com.hellmund.primetime.utils.plusAssign
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

sealed class Action {
    data class LoadMovies(
        val type: RecommendationsType = RecommendationsType.Personalized(),
        val page: Int
    ) : Action()

    data class StoreRating(val rating: Rating) : Action()
    data class Filter(val genres: List<Genre>) : Action()
}

sealed class Result {
    object Loading : Result()
    data class Data(
        val type: RecommendationsType,
        val data: List<MovieViewEntity>,
        val page: Int
    ) : Result()

    data class Error(val error: Throwable) : Result()
    data class RatingStored(val movie: MovieViewEntity) : Result()
    data class Filter(val genres: List<Genre>) : Result()
}

class MainViewModel @Inject constructor(
    private val repository: MoviesRepository,
    private val historyRepository: HistoryRepository,
    private val rankingProcessor: MovieRankingProcessor,
    private val viewEntityMapper: MoviesViewEntityMapper,
    private val recommendationsType: RecommendationsType
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val refreshRelay = PublishRelay.create<Action>()

    private val _viewState = MutableLiveData<MainViewState>()
    val viewState: LiveData<MainViewState> = _viewState

    private var pagesLoaded: Int = 0
    private var isLoadingMore: Boolean = false

    init {
        val initialViewState = MainViewState(isLoading = true)
        compositeDisposable += refreshRelay
            .switchMap(this::processAction)
            .scan(initialViewState, this::reduceState)
            .subscribe(this::render)
        refresh()
    }

    private fun processAction(action: Action): Observable<Result> {
        return when (action) {
            is Action.LoadMovies -> fetchRecommendations(action.type, action.page)
            is Action.StoreRating -> storeRating(action.rating)
            is Action.Filter -> Observable.just(Result.Filter(action.genres))
        }
    }

    private fun fetchRecommendations(
        type: RecommendationsType,
        page: Int
    ): Observable<Result> {
        return repository.fetchRecommendations(type, page)
            .subscribeOn(Schedulers.io())
            .map { rankingProcessor.rank(it, type) }
            .map(viewEntityMapper)
            .map { Result.Data(type, it, page) as Result }
            .onErrorReturn { Result.Error(it) }
            .startWith(Result.Loading)
    }

    private fun storeRating(rating: Rating): Observable<Result> {
        val historyMovie = HistoryMovie.fromRating(rating)
        return historyRepository
            .store(historyMovie)
            .andThen(Observable.just(Result.RatingStored(rating.movie) as Result))
    }

    private fun reduceState(
        viewState: MainViewState,
        result: Result
    ): MainViewState {
        return when (result) {
            is Result.Loading -> viewState.toLoading()
            is Result.Data -> viewState.toData(result).also { pagesLoaded = it.pagesLoaded }
            is Result.Error -> viewState.toError(result.error)
            is Result.RatingStored -> viewState.toData(result)
            is Result.Filter -> viewState.toFiltered(result)
        }
    }

    private fun render(viewState: MainViewState) {
        isLoadingMore = false
        _viewState.postValue(viewState)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    fun refresh(
        page: Int = pagesLoaded + 1
    ) {
        if (isLoadingMore.not()) {
            isLoadingMore = true
            refreshRelay.accept(Action.LoadMovies(recommendationsType, page))
        }
    }

    fun filter(genres: List<Genre>) {
        refreshRelay.accept(Action.Filter(genres))
    }

    fun handleRating(rating: Rating) {
        refreshRelay.accept(Action.StoreRating(rating))
    }

}
