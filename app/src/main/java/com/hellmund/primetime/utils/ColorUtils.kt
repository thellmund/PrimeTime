package com.hellmund.primetime.utils

import android.graphics.Color
import android.support.annotation.ColorInt

object ColorUtils {

    @JvmStatic
    fun darken(@ColorInt color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] *= 0.8f
        return Color.HSVToColor(hsv)
    }

}
