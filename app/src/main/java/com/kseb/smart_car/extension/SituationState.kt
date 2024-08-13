package com.kseb.smart_car.extension

import com.kseb.smart_car.data.responseDto.ResponseSituationDto

sealed class SituationState {
    data object Loading:SituationState()
    data class Success(val situationDto:ResponseSituationDto):SituationState()
    data class Error(val message:String):SituationState()
}