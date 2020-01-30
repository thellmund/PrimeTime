package com.hellmund.primetime.onboarding.ui.selectgenres

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import com.google.android.material.chip.Chip
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.onboarding.R

class GenreChip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : Chip(context, attrs, defStyle) {

    private lateinit var _genre: Genre
    var genre: Genre
        get() = _genre
        set(value) {
            _genre = value
            text = value.name
        }

    init {
        isCheckable = true
        isClickable = true
        isCheckedIconVisible = false
        setTextColor(Color.WHITE)
        textSize = 15f
        setChipBackgroundColorResource(R.color.selector_genre_chip_background)
    }
}
