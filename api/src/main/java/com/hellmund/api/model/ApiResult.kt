package com.hellmund.api.model

sealed class ApiResult<T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Failure<T>(val error: Throwable? = null) : ApiResult<T>()
}
