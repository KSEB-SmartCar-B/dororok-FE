package com.kseb.smart_car.domain.repository

import com.kseb.smart_car.data.responseDto.ResponseAccessDto
import com.kseb.smart_car.data.responseDto.ResponseIsSignedDto
import com.kseb.smart_car.data.responseDto.ResponseSignInDto

interface AuthRepository {
    suspend fun getAccessToken(
        token:String,
    ):Result<ResponseAccessDto>

    suspend fun isSigned(
        token:String
    ):Result<ResponseIsSignedDto>

    suspend fun getSignIn(
        token:String
    ):Result<ResponseSignInDto>
}