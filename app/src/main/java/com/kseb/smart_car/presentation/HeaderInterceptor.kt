package com.kseb.smart_car.presentation

import android.content.SharedPreferences
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class HeaderInterceptor @Inject constructor(
    private val prefs: SharedPreferences
): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if(chain.request().headers["Auth"] == "false"){
            val newRequest = chain.request().newBuilder()
                .removeHeader("Auth")
                .build()
            return chain.proceed(newRequest)
        }

        var token = ""
        runBlocking {
            token = ("Bearer " + prefs.getString("kakaoToken", ""))
        }
        val newRequest = chain.request().newBuilder()
            .addHeader("Authorization", token)
            .build()
        val response = chain.proceed(newRequest)


        return response
    }
}