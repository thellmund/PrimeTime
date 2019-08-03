package com.hellmund.primetime.ui.shared

import androidx.lifecycle.MutableLiveData
import com.hellmund.primetime.ui.suggestions.RecommendationsType

class NavigationEventsStore : ViewEventsStore<NavigationEvent>()

class ReactiveStore<Event> {

    val event = MutableLiveData<Event>()

    fun dispatch(event: Event) {
        this.event.value = event
    }

}

class NavigationEvent(
    value: RecommendationsType
) : SingleLiveDataEvent<RecommendationsType>(value)

open class SingleLiveDataEvent<T>(
    private val value: T
) {

    private var hasBeenHandled = false

    fun getIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            value
        }
    }

}
