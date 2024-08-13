package com.kseb.smart_car.extension

import com.kseb.smart_car.data.responseDto.ResponseFavoritePlaceDto

sealed class GetFavoritePlaceState {
    data object Loading:GetFavoritePlaceState()
    data class Success(val favoritePlaceDto: ResponseFavoritePlaceDto):GetFavoritePlaceState()
    data class Error(val message:String):GetFavoritePlaceState()
}