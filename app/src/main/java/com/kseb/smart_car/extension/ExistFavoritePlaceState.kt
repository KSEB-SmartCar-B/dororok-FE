package com.kseb.smart_car.extension

sealed class ExistFavoritePlaceState {
    data object Loading:ExistFavoritePlaceState()
    data class Success(val response :Boolean):ExistFavoritePlaceState()
    data class Error(val message:String):ExistFavoritePlaceState()
}