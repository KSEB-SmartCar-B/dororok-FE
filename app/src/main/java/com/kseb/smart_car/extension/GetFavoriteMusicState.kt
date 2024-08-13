package com.kseb.smart_car.extension

import com.kseb.smart_car.data.responseDto.ResponseFavoriteMusicDto

sealed class GetFavoriteMusicState {
    data object Loading:GetFavoriteMusicState()
    data class Success(val favoriteMusicDto: ResponseFavoriteMusicDto):GetFavoriteMusicState()
    data class Error(val message:String):GetFavoriteMusicState()
}