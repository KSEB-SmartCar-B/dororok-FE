package com.kseb.smart_car.data.service

import com.kseb.smart_car.data.requestDto.RequestAccessDto
import com.kseb.smart_car.data.responseDto.ResponseAccessDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/users")
    suspend fun getAccessToken(
        @Body requestAccessDto: RequestAccessDto
    ): ResponseAccessDto
}