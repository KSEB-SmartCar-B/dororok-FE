package com.kseb.smart_car.extension

import com.kseb.smart_car.data.responseDto.ResponseSearchListDto

sealed class GetSearchState {
    data object Loading:GetSearchState()
    data class Success(val searchDto: ResponseSearchListDto):GetSearchState()
    data class Error(val message:String):GetSearchState()
}