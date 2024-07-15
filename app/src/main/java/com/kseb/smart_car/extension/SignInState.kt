package com.kseb.smart_car.extension

sealed class SignInState {
    data object Loading:SignInState()
    data class Success(val isSigned:Boolean):SignInState()
    data class Error(val message:String):SignInState()
}