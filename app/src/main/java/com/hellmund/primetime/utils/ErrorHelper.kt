package com.hellmund.primetime.utils

import io.reactivex.functions.Consumer
import timber.log.Timber

object ErrorHelper {
    fun logAndIgnore() = Consumer<Throwable> { t -> Timber.e(t) }
}
