package com.kseb.smart_car.presentation

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.kakao.vectormap.KakaoMapSdk
import com.kseb.smart_car.BuildConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application(){
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, BuildConfig.NATIVE_APP_KEY)
        KakaoMapSdk.init(this, BuildConfig.NATIVE_APP_KEY);
    }
}