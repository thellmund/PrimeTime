package com.hellmund.primetime.core

import android.content.Context
import androidx.annotation.StringRes
import javax.inject.Inject

interface StringProvider {
    fun getString(@StringRes resId: Int): String
}

class RealStringProvider @Inject constructor(
    private val context: Context
) : StringProvider {

    override fun getString(resId: Int): String = context.getString(resId)

}
