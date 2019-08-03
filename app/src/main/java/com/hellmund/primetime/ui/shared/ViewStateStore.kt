package com.hellmund.primetime.ui.shared

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.hellmund.primetime.utils.observe

abstract class ViewStateStore<State, Result>(
    private val initialState: State,
    private val reducer: Reducer<State, Result>
) {

    val viewState = MutableLiveData<State>().apply {
        value = initialState
    }

    fun observe(
        owner: LifecycleOwner,
        observer: (State) -> Unit
    ) = viewState.observe(owner) { observer(it) }

    private fun dispatchState(
        state: State
    ) {
        viewState.value = state
    }

    fun dispatch(
        result: Result
    ) {
        val state = reducer(state(), result)
        dispatchState(state)
    }

    private fun state() = viewState.value!!

}

interface Reducer<State, Result> {

    operator fun invoke(
        state: State,
        result: Result
    ): State

}
