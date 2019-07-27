package com.hellmund.primetime.utils

import android.app.ProgressDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.threeten.bp.LocalDate

fun <T> LiveData<T>.observe(owner: LifecycleOwner, block: (T) -> Unit) {
    observe(owner, Observer { block(it) })
}

fun <T> LiveData<T>.observeNonNull(owner: LifecycleOwner, block: (T) -> Unit) {
    observe(owner, Observer { it?.let(block) })
}

operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
    add(disposable)
}

val LocalDate.isAfterNow: Boolean
    get() = isAfter(LocalDate.now())

var ProgressDialog.isVisible: Boolean
    get() = isShowing
    set(value) {
        if (value) show() else dismiss()
    }

val Fragment.supportActionBar: ActionBar?
    get() = (requireActivity() as? AppCompatActivity)?.supportActionBar

fun <T> Set<T>.containsAny(elements: Collection<T>): Boolean = elements.any { contains(it) }

fun RecyclerView.onBottomReached(block: () -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val isAtBottom = recyclerView.canScrollVertically(1).not()

            if (isAtBottom) {
                block()
            }
        }
    })
}

val FragmentManager.backStack: List<FragmentManager.BackStackEntry>
    get() = (0 until backStackEntryCount).map {
        getBackStackEntryAt(it)
    }

fun ViewGroup.inflate(@LayoutRes resource: Int, attach: Boolean = true): View {
    return LayoutInflater.from(context).inflate(resource, this, attach)
}
