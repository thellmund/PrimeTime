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
}

sealed class ViewModelEvent {
    data class AdditionalInformationLoaded(val movie: ApiMovie) : ViewModelEvent()
    object TrailerLoading : ViewModelEvent()
    data class TrailerLoaded(val url: String) : ViewModelEvent()
    data class ImdbLinkLoaded(val url: String) : ViewModelEvent()
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

    fun loadTrailer() {
        actionsRelay.accept(ViewModelAction.LoadTrailer)
    }

    fun loadAdditionalInformation() {
        actionsRelay.accept(ViewModelAction.LoadAdditionalInformation)
    }

    fun openImdb() {
        actionsRelay.accept(ViewModelAction.OpenImdb)
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
