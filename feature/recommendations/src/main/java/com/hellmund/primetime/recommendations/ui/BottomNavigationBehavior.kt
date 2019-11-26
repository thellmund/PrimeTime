package com.hellmund.primetime.recommendations.ui

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams

annotation class ScrollAwareFragment

class BottomNavigationBehavior : CoordinatorLayout.Behavior<View> {

    private lateinit var contentFrame: FrameLayout
    private var bottomNavigationHeight: Int = 0

    private var isHiding = false
    private var isShowing = false

    private enum class ScrollDirection {
        UP, DOWN, NONE
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

    private var contentFrameAnimator: ValueAnimator? = null
    private var currentScrollDirection = ScrollDirection.NONE

    private fun handleScrollDirection(
        scrollDirection: ScrollDirection,
        child: View
    ) {
        val shouldHideNavigation = scrollDirection == ScrollDirection.UP && !isHiding
        val shouldShowNavigation = scrollDirection == ScrollDirection.DOWN && !isShowing

        if (scrollDirection != currentScrollDirection) {
            currentScrollDirection = scrollDirection
            updateBottomMargin(shouldShowNavigation)
        }

        if (shouldHideNavigation) {
            isHiding = true
            isShowing = false
            child.animate().translationY(child.height.toFloat())
        } else if (shouldShowNavigation) {
            isShowing = true
            isHiding = false
            child.animate().translationY(0f)
        }
    }

    private fun updateBottomMargin(
        shouldShowNavigation: Boolean
    ) {
        val marginParams = contentFrame.layoutParams as ViewGroup.MarginLayoutParams

        val currentBottomMargin = marginParams.bottomMargin
        val desiredBottomMargin = when {
            shouldShowNavigation -> bottomNavigationHeight
            else -> 0
        }

        contentFrameAnimator?.end()
        contentFrameAnimator = contentFrame.animateBottomMarginChange(
            from = currentBottomMargin,
            to = desiredBottomMargin,
            block = { newBottomMargin ->
                marginParams.bottomMargin = newBottomMargin
            }
        )
    }

    private fun ViewGroup.animateBottomMarginChange(
        from: Int,
        to: Int,
        block: ViewGroup.LayoutParams.(Int) -> Unit
    ): ValueAnimator {
        val animator = ValueAnimator.ofInt(from, to)
        animator.addUpdateListener {
            val value = it.animatedValue as Int
            updateLayoutParams {
                block(value)
            }
        }
        animator.start()
        return animator
    }

    companion object {
        fun with(
            contentFrame: FrameLayout,
            bottomNavigationHeight: Int
        ) = BottomNavigationBehavior().apply {
            this.contentFrame = contentFrame
            this.bottomNavigationHeight = bottomNavigationHeight
        }
    }
}
