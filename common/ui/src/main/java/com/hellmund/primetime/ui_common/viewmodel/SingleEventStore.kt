package com.hellmund.primetime.ui_common.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe

open class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    fun ifNotHandled(block: (T) -> Unit) {
        if (!hasBeenHandled) {
            hasBeenHandled = true
            block(content)
        }
    }
}

fun <T> LiveData<Event<T>>.handle(owner: LifecycleOwner, observer: (T) -> Unit) {
    observe(owner) { uiEvent ->
        uiEvent.ifNotHandled { content ->
            observer.invoke(content)
        }
    }
}

class SingleEventStore<T> {

    private val _events = MutableLiveData<Event<T>>()
    val events: LiveData<Event<T>> = _events

    fun dispatch(content: T) {
        _events.value = Event(content)
    }
}
