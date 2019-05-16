package com.hellmund.primetime.utils.viewpagerindicator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.viewpager2.widget.ViewPager2

class ViewPagerIndicator @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var _viewPager: ViewPager2? = null
    private var currentPosition = 0

    private var selectedPaint = Paint().apply {
        color = Color.WHITE
    }

    private var unselectedPaint = Paint().apply {
        color = Color.GRAY
    }

    private val callback = object : ViewPager2.OnPageChangeCallback() {

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            currentPosition = position
            invalidate()
        }

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val viewPager = _viewPager ?: return
        val count = viewPager.adapter?.itemCount ?: return

        if (currentPosition >= count) {
            currentPosition = count - 1
            return
        }

        val radius = 8 // TODO
        val diameter = radius * 2

        val usedWidth = count * diameter + (count - 1) * diameter
        var startX = (width - usedWidth) / 2

        for (position in 0 until count) {
            val paint = if (position == currentPosition) selectedPaint else unselectedPaint
            canvas?.drawCircle(startX.toFloat(), height / 2f, radius.toFloat(), paint)
            startX += (diameter * 2)
        }
    }

    fun setViewPager(viewPager: ViewPager2) {
        if (_viewPager == viewPager) {
            return
        }

        _viewPager?.unregisterOnPageChangeCallback(callback)

        _viewPager = viewPager
        _viewPager?.registerOnPageChangeCallback(callback)
        invalidate()
    }



}
