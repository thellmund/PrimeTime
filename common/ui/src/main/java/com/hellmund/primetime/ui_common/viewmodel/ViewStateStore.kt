package com.hellmund.primetime.ui_common.viewmodel

import androidx.lifecycle.MutableLiveData

fun <State, Result> viewStateStore(
    initialState: State,
    reducer: Reducer<State, Result>
): ViewStateStore<State, Result> = ViewStateStore(initialState, reducer)

class ViewStateStore<State, Result>(
    private val initialState: State,
    private val reducer: Reducer<State, Result>
) {

    val viewState = MutableLiveData<State>().apply {
        value = initialState
    }

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

    operator fun plusAssign(result: Result) {
        dispatch(result)
    }

    private fun state() = viewState.value!!
}

interface Reducer<State, Result> {

    operator fun invoke(
        state: State,
        viewResult: Result
    ): State
}
