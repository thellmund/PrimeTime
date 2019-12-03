package com.hellmund.primetime.ui_common.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe

open class SingleEvent<out T>(private val content: T) {
    private var hasBeenHandled = false

    fun ifNotHandled(block: (T) -> Unit) {
        if (!hasBeenHandled) {
            hasBeenHandled = true
            block(content)
        }
    }
}

fun <T> LiveData<SingleEvent<T>>.observeSingleEvents(owner: LifecycleOwner, observer: (T) -> Unit) {
    observe(owner) { uiEvent ->
        uiEvent.ifNotHandled { content ->
            observer.invoke(content)
        }
    }
}

class SingleEventStore<T> {

    private val _events = MutableLiveData<SingleEvent<T>>()
    val events: LiveData<SingleEvent<T>> = _events

    fun dispatch(content: T) {
        _events.value = SingleEvent(content)
    }
}
