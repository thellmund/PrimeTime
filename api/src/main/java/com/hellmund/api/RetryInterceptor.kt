package com.hellmund.api

import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

class RetryInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)

        var statusCode = response.code
        while (statusCode == 429) {
            // Too many requests
            val retryAfter = response.header("Retry-After")?.toIntOrNull() ?: 10
            Thread.sleep(retryAfter * 1_000L)

            response = chain.proceed(request)
            statusCode = response.code
        }

        return response
    }
}
