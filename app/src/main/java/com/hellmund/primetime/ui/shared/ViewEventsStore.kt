package com.hellmund.primetime.ui.shared

import androidx.lifecycle.MutableLiveData

abstract class ViewEventsStore<Event> {

    val viewEvents = MutableLiveData<Event>()

    fun dispatch(
        event: Event
    ) {
        viewEvents.value = event
    }

}
