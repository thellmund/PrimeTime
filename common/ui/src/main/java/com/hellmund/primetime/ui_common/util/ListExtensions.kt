package com.hellmund.primetime.ui_common.util

fun <T> List<T>.replace(index: Int, element: T): List<T> {
    return toMutableList().apply {
        removeAt(index)
        add(index, element)
    }
}
