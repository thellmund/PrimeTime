package com.hellmund.primetime.utils

import io.reactivex.Observable
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.rx2.openSubscription

@ObsoleteCoroutinesApi
@FlowPreview
fun <T> Observable<T>.asFlow(): Flow<T> = openSubscription().consumeAsFlow()
