package com.hellmund.primetime.api

import com.hellmund.primetime.utils.ApiUtils
import okhttp3.Interceptor
import okhttp3.Response

class TmdbInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url()

        val modifiedUrl = url.newBuilder()
                .addQueryParameter("api_key", ApiUtils.apiKey)
                .addQueryParameter("language", ApiUtils.language)
                .build()

        val modifiedRequest = request.newBuilder().url(modifiedUrl).build()
        return chain.proceed(modifiedRequest)
    }

}
