package com.hellmund.primetime.ui_common.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <T> LiveData<T>.observe(owner: LifecycleOwner, block: (T) -> Unit) {
    observe(owner, Observer { block(it) })
}

fun <T> List<T>.replace(index: Int, element: T): List<T> {
    return toMutableList().apply {
        removeAt(index)
        add(index, element)
    }
}
