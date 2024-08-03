package com.kseb.smart_car.extension

import com.kseb.smart_car.data.responseDto.ResponseAddSearchDto
import com.kseb.smart_car.data.responseDto.ResponseSearchListDto

sealed class AddSearchState {
    data object Loading:AddSearchState()
    data class Success(val searchDto: ResponseAddSearchDto):AddSearchState()
    data class Error(val message:String):AddSearchState()
}