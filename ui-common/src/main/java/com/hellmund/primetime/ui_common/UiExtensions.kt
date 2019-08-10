@file:JvmName("UiUtils")

package com.hellmund.primetime.ui_common

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

@JvmOverloads
fun Context.showToast(resId: Int, length: Int = Toast.LENGTH_SHORT) {
    showToast(getString(resId), length)
}

@JvmOverloads
fun Context.showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
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
