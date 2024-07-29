package com.kseb.smart_car.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.kseb.smart_car.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Converter
import retrofit2.Converter.Factory
import retrofit2.Retrofit
import timber.log.Timber
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import kotlin.annotation.AnnotationRetention.BINARY
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        interceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(300, TimeUnit.SECONDS).readTimeout(200, TimeUnit.SECONDS)
            .writeTimeout(200, TimeUnit.SECONDS).build()
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            when {
                message.isJsonObject() ->
                    Timber.tag("okhttp").d(JSONObject(message).toString(4))

                message.isJsonArray() ->
                    Timber.tag("okhttp").d(JSONObject(message).toString(4))

                else -> {
                    Timber.tag("okhttp").d("CONNECTION INFO -> $message")
                }
            }
        }
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return loggingInterceptor
    }

    @Provides
    @Singleton
    @BaseUrlRetrofit
    fun provideBaseRetrofit(
        jsonConverter: Converter.Factory,
        client: OkHttpClient,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(jsonConverter)
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    @KakaoMapRetrofit
    fun provideKakaoMapRetrofit(
        jsonConverter: Factory,
        client: OkHttpClient,
    ):Retrofit{
        return Retrofit.Builder()
            .baseUrl(BuildConfig.KAKAO_MAP_URL)
            .addConverterFactory(jsonConverter)
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    fun provideJsonConverterFactory(): Converter.Factory {
        return Json.asConverterFactory("application/json".toMediaType())
    }


}
fun String?.isJsonObject(): Boolean = this?.startsWith("{") == true && this.endsWith("}")
fun String?.isJsonArray(): Boolean = this?.startsWith("[") == true && this.endsWith("]")


@Qualifier
@Retention(RetentionPolicy.RUNTIME)
annotation class BaseUrlRetrofit

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
annotation class KakaoMapRetrofit