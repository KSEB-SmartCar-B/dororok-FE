package com.kseb.smart_car.presentation

import android.app.Application
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.KakaoSdk.appKey
import com.kakaomobility.knsdk.KNLanguageType
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.common.objects.KNError_Code_C302
import com.kseb.smart_car.BuildConfig
import com.kseb.smart_car.BuildConfig.VERSION_NAME
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application(){
    companion object {
        lateinit var instance: MyApp
            private set
    }
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, BuildConfig.NATIVE_APP_KEY)
        //KakaoMapSdk.init(this, BuildConfig.NATIVE_APP_KEY);
        KNSDK.install(this, "$filesDir/knsdk")
    }
}