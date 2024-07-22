package com.kseb.smart_car.data.datasource

import com.kseb.smart_car.data.requestDto.RequestAccessDto
import com.kseb.smart_car.data.requestDto.RequestSignUpDto
import com.kseb.smart_car.data.responseDto.ResponseAccessDto
import com.kseb.smart_car.data.responseDto.ResponseIsSignedDto
import com.kseb.smart_car.data.responseDto.ResponseSignInDto
import com.kseb.smart_car.data.responseDto.ResponseSignUpDto

interface AuthDataSource {
    suspend fun getAccessToken(
        token: RequestAccessDto
    ): ResponseAccessDto

    suspend fun isSigned(
        token:String
    ):ResponseIsSignedDto

    suspend fun getSignIn(
        token:String
    ): ResponseSignInDto

    suspend fun getSignUp(
        info:RequestSignUpDto
    ):ResponseSignUpDto
}