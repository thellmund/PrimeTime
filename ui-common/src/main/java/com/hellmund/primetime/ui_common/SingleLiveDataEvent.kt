package com.hellmund.primetime.ui_common

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
