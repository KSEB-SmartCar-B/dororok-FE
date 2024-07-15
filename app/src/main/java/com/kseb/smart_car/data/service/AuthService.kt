package com.kseb.smart_car.data.service

import com.kseb.smart_car.data.requestDto.RequestAccessDto
import com.kseb.smart_car.data.responseDto.ResponseAccessDto
import com.kseb.smart_car.data.responseDto.ResponseIsSignedDto
import com.kseb.smart_car.data.responseDto.ResponseSignInDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/users")
    suspend fun getAccessToken(
        @Body requestAccessDto: RequestAccessDto
    ): ResponseAccessDto

    @POST("members/isSigned")
    suspend fun isSigned(
        @Body kakaoAccessToken: String
    ): ResponseIsSignedDto
    @POST("/members/sign-in")
    suspend fun getSignIn(
        @Body kakaoAccessToken:String
    ): ResponseSignInDto
}