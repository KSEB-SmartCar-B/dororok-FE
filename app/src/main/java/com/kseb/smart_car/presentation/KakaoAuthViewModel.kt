package com.kseb.smart_car.presentation

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class KakaoAuthViewModel(
    application: Application,
    val allViewModel:AllViewModel
) : AndroidViewModel(application) {
    companion object {
        const val TAG = "KakaoAuthViewModel"
    }

    private val context = application.applicationContext
    val isLoggedIn = MutableStateFlow<Boolean>(false)

    fun kakaoLogin(activityContext: Context) {
        viewModelScope.launch {
            //handleKakaoLogin()
            isLoggedIn.emit(handleKakaoLogin(activityContext))
        }
    }

    private suspend fun handleKakaoLogin(activityContext: Context): Boolean =
        suspendCoroutine { continuation ->
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오계정으로 로그인 실패 ${error.message}", error)
                    continuation.resume(false)
                } else if (token != null) {
                    Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
                    allViewModel.getAccessToken(token.accessToken)
                    continuation.resume(true)
                }
            }

            if (UserApiClient.instance.isKakaoTalkLoginAvailable(activityContext)) {
                Log.d(TAG, "카카오톡 available")
                UserApiClient.instance.loginWithKakaoTalk(activityContext) { token, error ->
                    if (error != null) {
                        Log.e(TAG, "카카오톡으로 로그인 실패 ${error.message}", error)

                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            continuation.resume(false)
                            return@loginWithKakaoTalk
                        }

                        UserApiClient.instance.loginWithKakaoAccount(activityContext, callback = callback)
                    } else if (token != null) {
                        Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
                        allViewModel.getAccessToken(token.accessToken)
                        continuation.resume(true)
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(activityContext, callback = callback)
            }
        }

}