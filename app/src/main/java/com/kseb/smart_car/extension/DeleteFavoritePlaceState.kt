package com.kseb.smart_car.extension

sealed class DeleteFavoritePlaceState {
    data object Loading:DeleteFavoritePlaceState()
    data class Success(val response :String):DeleteFavoritePlaceState()
    data class Error(val message:String):DeleteFavoritePlaceState()
}