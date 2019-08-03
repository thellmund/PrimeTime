package com.hellmund.primetime.ui.shared

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
