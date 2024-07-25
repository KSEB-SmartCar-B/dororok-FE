package com.kseb.smart_car.extension

sealed class SignUpState {
    data object Loading:SignUpState()
    data class Success(val accessToken:String):SignUpState()
    data class Error(val message:String):SignUpState()
}