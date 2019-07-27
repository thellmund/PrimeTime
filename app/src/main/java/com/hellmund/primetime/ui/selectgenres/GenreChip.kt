package com.hellmund.primetime.ui.selectgenres

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import com.google.android.material.chip.Chip
import com.hellmund.primetime.R
import org.jetbrains.anko.textColor

class GenreChip @JvmOverloads constructor(
    context: Context,
    title: String = "",
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : Chip(context, attrs, defStyle) {

    init {
        text = title
        isCheckable = true
        isClickable = true
        isCheckedIconVisible = false
        textColor = Color.WHITE
        textSize = 15f
        setChipBackgroundColorResource(R.color.selector_genre_chip_background)
    }

}
