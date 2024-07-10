package com.kseb.smart_car.domain.repository

import com.kseb.smart_car.data.responseDto.ResponseAccessDto

interface AuthRepository {
    suspend fun getAccessToken(
        token:String,
    ):Result<ResponseAccessDto>
}