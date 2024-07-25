package com.kseb.smart_car.domain.repository

import com.kseb.smart_car.data.requestDto.RequestSignUpDto
import com.kseb.smart_car.data.responseDto.ResponseAccessDto
import com.kseb.smart_car.data.responseDto.ResponseIsSignedDto
import com.kseb.smart_car.data.responseDto.ResponseMyInfoDto
import com.kseb.smart_car.data.responseDto.ResponseSignInDto
import com.kseb.smart_car.data.responseDto.ResponseSignUpDto

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

    suspend fun getSignUp(
        info:RequestSignUpDto
    ):Result<ResponseSignUpDto>

    suspend fun getMyInfo(
        token:String
    ):Result<ResponseMyInfoDto>
}