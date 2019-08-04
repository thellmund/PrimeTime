package com.hellmund.primetime.utils

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.threeten.bp.LocalDate

fun <T> LiveData<T>.observe(owner: LifecycleOwner, block: (T) -> Unit) {
    observe(owner, Observer { block(it) })
}

val LocalDate.isAfterNow: Boolean
    get() = isAfter(LocalDate.now())

fun <T> List<T>.replace(index: Int, element: T): List<T> {
    return toMutableList().apply {
        removeAt(index)
        add(index, element)
    }
}
