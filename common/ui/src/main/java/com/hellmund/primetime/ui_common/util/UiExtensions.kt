@file:JvmName("UiUtils")

package com.hellmund.primetime.ui_common.util

import android.content.Context
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.pandora.bottomnavigator.BottomNavigator

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

fun Window.requestFullscreenLayout() {
    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION  or
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
}

val Fragment.navigator: BottomNavigator
    get() = BottomNavigator.provide(requireActivity())
