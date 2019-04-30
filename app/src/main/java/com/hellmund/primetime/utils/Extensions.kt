package com.hellmund.primetime.utils

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.support.v4.view.ViewPager
import android.util.SparseBooleanArray
import android.view.View
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun <T> LiveData<T>.observe(owner: LifecycleOwner, block: (T) -> Unit) {
    observe(owner, Observer { it?.let(block) })
}

operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
    add(disposable)
}

fun ViewPager.scrollToStart() {
    currentItem = 0
}

fun ViewPager.scrollToNext() {
    currentItem += 1
}

fun ViewPager.scrollToPrevious() {
    currentItem -= 1
}

var View.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }

fun SparseBooleanArray.toList(): List<Boolean> = (0 until size()).map { get(it) }
