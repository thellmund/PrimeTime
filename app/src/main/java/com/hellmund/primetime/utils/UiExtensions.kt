@file:JvmName("UiUtils")

package com.hellmund.primetime.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

fun Context.showToast(resId: Int, length: Int = Toast.LENGTH_SHORT) {
    showToast(getString(resId), length)
}

fun Fragment.showToast(resId: Int, length: Int = Toast.LENGTH_SHORT) {
    requireContext().showToast(resId, length)
}

fun Context.showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

fun Fragment.showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
    requireContext().showToast(message, length)
}

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

fun View.updatePadding(
    left: Int = paddingLeft,
    top: Int = paddingTop,
    right: Int = paddingRight,
    bottom: Int = paddingBottom
) {
    setPadding(left, top, right, bottom)
}
