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

sealed class ViewModelAction {
    object LoadAdditionalInformation : ViewModelAction()
    object LoadTrailer : ViewModelAction()
    object OpenImdb : ViewModelAction()
    data class StoreRating(val rating: Rating) : ViewModelAction()
}

sealed class ViewModelEvent {
    data class AdditionalInformationLoaded(val movie: ApiMovie) : ViewModelEvent()
    object TrailerLoading : ViewModelEvent()
    data class TrailerLoaded(val url: String) : ViewModelEvent()
    data class ImdbLinkLoaded(val url: String) : ViewModelEvent()
    data class RatingStored(val rating: Rating) : ViewModelEvent()
}

sealed class Rating(val movie: ApiMovie) {
    class Like(movie: ApiMovie) : Rating(movie)
    class Dislike(movie: ApiMovie) : Rating(movie)
}

class SuggestionsViewModel(
        private val repository: RecommendationsRepository,
        private var movie: ApiMovie
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val actionsRelay = PublishRelay.create<ViewModelAction>()

    private val _viewModelEvents = MutableLiveData<ViewModelEvent>()
    val viewModelEvents: LiveData<ViewModelEvent> = _viewModelEvents

    init {
        compositeDisposable += actionsRelay
                .switchMap(this::processAction)
                .subscribe(this::render)
    }

    private fun processAction(action: ViewModelAction): Observable<ViewModelEvent> {
        return when (action) {
            is ViewModelAction.LoadAdditionalInformation -> fetchInformation()
            is ViewModelAction.LoadTrailer -> fetchTrailer()
            is ViewModelAction.OpenImdb -> fetchImdbLink()
            is ViewModelAction.StoreRating -> storeRating(action.rating)
        }
    }

    private fun fetchInformation(): Observable<ViewModelEvent> {
        return repository
                .fetchMovie(movie.id)
                .doOnNext {
                    movie = it
                }
                .map { ViewModelEvent.AdditionalInformationLoaded(it) }
    }

    private fun fetchTrailer(): Observable<ViewModelEvent> {
        return repository
                .fetchVideo(movie)
                .startWith { ViewModelEvent.TrailerLoading }
                .map { ViewModelEvent.TrailerLoaded(it) }
    }

    private fun fetchImdbLink(): Observable<ViewModelEvent> {
        val url = "http://www.imdb.com/title/${movie.imdbId}"
        return Observable.just(ViewModelEvent.ImdbLinkLoaded(url))
    }

    private fun storeRating(rating: Rating): Observable<ViewModelEvent> {
        // TODO: Actually store the rating
        return Observable.just(ViewModelEvent.RatingStored(rating))
    }

    fun loadTrailer() {
        actionsRelay.accept(ViewModelAction.LoadTrailer)
    }

    fun loadAdditionalInformation() {
        actionsRelay.accept(ViewModelAction.LoadAdditionalInformation)
    }

    fun openImdb() {
        actionsRelay.accept(ViewModelAction.OpenImdb)
    }

    fun handleRating(which: Int) {
        val rating = if (which == 0) Rating.Like(movie) else Rating.Dislike(movie)
        actionsRelay.accept(ViewModelAction.StoreRating(rating))
    }

    private fun render(event: ViewModelEvent) {
        _viewModelEvents.postValue(event)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    class Factory(
            private val repository: RecommendationsRepository,
            private val movie: ApiMovie
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SuggestionsViewModel(repository, movie) as T
        }

    }

}
