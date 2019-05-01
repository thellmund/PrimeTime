package com.hellmund.primetime.utils

import android.app.ProgressDialog
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.google.android.material.button.MaterialButton
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.threeten.bp.LocalDate

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

val LocalDate.isAfterNow: Boolean
    get() = isAfter(LocalDate.now())

var ProgressDialog.isVisible: Boolean
    get() = isShowing
    set(value) {
        if (value) show() else dismiss()
    }

val MaterialButton.margins: ViewGroup.MarginLayoutParams?
    get() = layoutParams as? ViewGroup.MarginLayoutParams
