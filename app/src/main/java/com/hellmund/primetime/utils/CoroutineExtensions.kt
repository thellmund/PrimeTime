package com.hellmund.primetime.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList

suspend fun <T> Flow<List<T>>.collectFirst(): List<T> = toList().flatten()
