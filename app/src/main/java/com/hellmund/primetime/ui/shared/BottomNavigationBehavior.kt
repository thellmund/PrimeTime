package com.hellmund.primetime.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat

annotation class ScrollAwareFragment

class BottomNavigationBehavior : CoordinatorLayout.Behavior<View> {

    private var isHiding = false
    private var isShowing = false

    private enum class ScrollDirection {
        UP, DOWN
    }

    constructor() : super()
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean = axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        val direction = if (dyConsumed > 0) ScrollDirection.UP else ScrollDirection.DOWN
        handleScrollDirection(direction, child)
    }

    private fun handleScrollDirection(
        scrollDirection: ScrollDirection,
        child: View
    ) {
        if (scrollDirection == ScrollDirection.UP && !isHiding) {
            isHiding = true
            isShowing = false
            child.animate().translationY(child.height.toFloat())
        } else if (scrollDirection == ScrollDirection.DOWN && !isShowing) {
            isShowing = true
            isHiding = false
            child.animate().translationY(0f)
        }
    }

}
