package com.kseb.smart_car.extension

import com.kseb.smart_car.data.responseDto.ResponseMyInfoDto
import com.kseb.smart_car.data.responseDto.ResponseUpdateInfoDto

sealed class UpdateInfoState {
    data object Loading:UpdateInfoState()
    data class Success(val infoDto: ResponseUpdateInfoDto):UpdateInfoState()
    data class Error(val message:String):UpdateInfoState()
}