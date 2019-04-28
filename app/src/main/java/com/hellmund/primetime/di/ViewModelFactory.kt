package com.hellmund.primetime.di

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import javax.inject.Provider

class ViewModelFactory<T : ViewModel>(
        private val provider: Provider<T>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return provider.get() as T
    }

}

fun <T : ViewModel> FragmentActivity.lazyViewModel(block: () -> Provider<T>): Lazy<T> = lazy {
    val factory = ViewModelFactory(block())
    ViewModelProviders.of(this, factory).get(block().get().javaClass)
}

fun <T : ViewModel> Fragment.lazyViewModel(block: () -> Provider<T>): Lazy<T> = lazy {
    val factory = ViewModelFactory(block())
    ViewModelProviders.of(this, factory).get(block().get().javaClass)
}
