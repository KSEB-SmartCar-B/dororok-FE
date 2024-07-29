package com.kseb.smart_car.extension

import com.kseb.smart_car.data.responseDto.ResponseAddressDto

sealed class AddressState {
    data object Loading:AddressState()
    data class Success(val addressDto: ResponseAddressDto):AddressState()
    data class Error(val message:String):AddressState()
}