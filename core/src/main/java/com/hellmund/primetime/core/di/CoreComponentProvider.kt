package com.hellmund.primetime.core.di

import android.content.Context
import androidx.fragment.app.Fragment

interface CoreComponentProvider {
    val coreComponent: CoreComponent
}

val Context.coreComponent: CoreComponent get() = (applicationContext as CoreComponentProvider).coreComponent
val Fragment.coreComponent: CoreComponent get() = requireActivity().coreComponent
