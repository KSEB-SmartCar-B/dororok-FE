package com.kseb.smart_car.extension

import com.kseb.smart_car.data.responseDto.ResponseDeleteSearchDto

sealed class DeleteSearchState {
    data object Loading:DeleteSearchState()
    data class Success(val searchDto: ResponseDeleteSearchDto):DeleteSearchState()
    data class Error(val message:String):DeleteSearchState()
}