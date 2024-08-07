import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android") version "2.51"
}

android {
    namespace = "com.kseb.smart_car"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.kseb.smart_car"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled=true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "NATIVE_APP_KEY",
            gradleLocalProperties(rootDir, providers).getProperty("kakao.native.app.key")
        )

        buildConfigField(
            "String",
            "KAKAO_REST_API",
            gradleLocalProperties(rootDir, providers).getProperty("kakao.rest.api.key")
        )

        buildConfigField(
            "String",
            "BASE_URL",
            gradleLocalProperties(rootDir, providers).getProperty("base.url")
        )

        buildConfigField(
            "String",
            "KAKAO_NAVI_URL",
            gradleLocalProperties(rootDir,providers).getProperty("kakao.navi.url")
        )

        buildConfigField(
            "String",
            "KAKAO_MAP_URL",
            gradleLocalProperties(rootDir,providers).getProperty("kakao.map.url")
        )

        resValue("string","kakao_oauth_host",gradleLocalProperties(rootDir, providers).getProperty("kakao.oauth.host"))
        resValue("string","kakao_app_key", gradleLocalProperties(rootDir,providers).getProperty("kakao.native.app.key"))
        resValue("string","kakao_rest_api_key", gradleLocalProperties(rootDir,providers).getProperty("kakao.rest.api.key"))

        ndk {
            abiFilters.add("armeabi, armeabi-v7a")
            abiFilters.add("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding {enable=true}
        buildConfig = true
        dataBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.location)
    implementation(libs.androidx.ui.test.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    // define a BOM and its version
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.10.0"))

    // define any required OkHttp artifacts without version
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    //viewmodel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    implementation("androidx.fragment:fragment-ktx:1.8.1")
    implementation("androidx.activity:activity-ktx:1.9.0")

    // coil
    implementation("io.coil-kt:coil:2.4.0")

    //hilt
    implementation("com.google.dagger:hilt-android:2.46.1")
    kapt("com.google.dagger:hilt-android-compiler:2.46.1")
    kapt("com.google.dagger:dagger-android-processor:2.46.1")

    // timber
    implementation("com.jakewharton.timber:timber:4.7.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.11.0")

    //kakao
    implementation("com.kakao.sdk:v2-user:2.20.1")

    //kakao map
    implementation ("com.kakao.maps.open:android:2.9.5")

    //kakaomobility
    implementation ("com.kakaomobility.knsdk:knsdk_ui:1.9.4")

    //gson
    implementation ("com.google.code.gson:gson:2.10.1")

    //glide
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")

    //spotify
    implementation(files("../app-remote-lib/spotify-app-remote-release-0.8.0.aar"))

    //coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3") // 최신 버전으로 설정
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3") // Android에서 코루틴을 사용하기 위한 라이브러리
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3") // 최신 버전으로 변경 가능

}

kapt {
    correctErrorTypes = true
}

hilt {
    enableAggregatingTask = false
}