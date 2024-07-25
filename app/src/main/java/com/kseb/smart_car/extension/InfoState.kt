package com.kseb.smart_car.extension

import com.kseb.smart_car.data.responseDto.ResponseMyInfoDto

sealed class InfoState {
    data object Loading:InfoState()
    data class Success(val infoDto: ResponseMyInfoDto):InfoState()
    data class Error(val message:String):InfoState()
}