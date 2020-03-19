@file:JvmName("UiUtils")

package com.hellmund.primetime.ui_common.util

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialContainerTransformSharedElementCallback
import com.pandora.bottomnavigator.BottomNavigator
import dev.chrisbanes.insetter.doOnApplyWindowInsets

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

fun Activity.requestFullscreenLayout() {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
}

fun View.updateTopMarginForFullscreenLayout() {
    doOnApplyWindowInsets { v, insets, initialState ->
        v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = initialState.margins.top + insets.systemWindowInsetTop
        }
    }
}

fun View.updateBottomPaddingForFullscreenLayout() {
    doOnApplyWindowInsets { v, insets, initialState ->
        v.updatePadding(
            bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom
        )
    }
}

val Fragment.navigator: BottomNavigator
    get() = BottomNavigator.provide(requireActivity())

/** apply material exit container transformation. */
fun AppCompatActivity.applyExitMaterialTransform() {
    window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
    setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
    window.sharedElementsUseOverlay = false
}

/** apply material entered container transformation. */
fun AppCompatActivity.applyMaterialTransform(transitionName: String) {
    window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
    ViewCompat.setTransitionName(findViewById<View>(android.R.id.content), transitionName)

    // set up shared element transition
    setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
    window.sharedElementEnterTransition = getContentTransform()
    window.sharedElementReturnTransition = getContentTransform()
}

/** get a material container arc transform. */
private fun AppCompatActivity.getContentTransform(): MaterialContainerTransform {
    return MaterialContainerTransform(this).apply {
        addTarget(android.R.id.content)
        duration = 450
        pathMotion = MaterialArcMotion()
    }
}

fun Activity.makeSceneTransitionAnimation(
    startView: View,
    sharedElementName: String
) = ActivityOptions.makeSceneTransitionAnimation(this, startView, sharedElementName)
