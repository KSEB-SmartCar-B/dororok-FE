package com.kseb.smart_car.extension

sealed class AddFavoritePlaceState {
    data object Loading:AddFavoritePlaceState()
    data class Success(val response :String):AddFavoritePlaceState()
    data class Error(val message:String):AddFavoritePlaceState()
}