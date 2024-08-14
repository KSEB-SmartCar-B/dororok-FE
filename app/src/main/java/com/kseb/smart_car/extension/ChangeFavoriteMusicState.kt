package com.kseb.smart_car.extension

import com.kseb.smart_car.data.responseDto.ResponseFavoriteMusicDto

sealed class ChangeFavoriteMusicState {
    data object Loading:ChangeFavoriteMusicState()
    data class Success(val response:String):ChangeFavoriteMusicState()
    data class Error(val message:String):ChangeFavoriteMusicState()
}