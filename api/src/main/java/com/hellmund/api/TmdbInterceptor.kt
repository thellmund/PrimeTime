package com.hellmund.api

import okhttp3.Interceptor
import okhttp3.Response
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmdbInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url()

        val modifiedUrl = url.newBuilder()
            .addQueryParameter("api_key", "7564dba629324e3048f362a03c8a76bc") // TODO
            .addQueryParameter("language", Locale.getDefault().language)
            .build()

        val modifiedRequest = request.newBuilder().url(modifiedUrl).build()
        return chain.proceed(modifiedRequest)
    }

}
