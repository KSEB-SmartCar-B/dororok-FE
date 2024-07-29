package com.kseb.smart_car.domain.repository

import com.kseb.smart_car.data.responseDto.ResponseAddressDto

interface KakaoRepository {
    suspend fun getAddress(
        token:String,
        address:String
    ):Result<ResponseAddressDto>
}