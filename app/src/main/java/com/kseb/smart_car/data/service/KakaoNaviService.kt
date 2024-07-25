package com.kseb.smart_car.data.service

import com.kseb.smart_car.data.requestDto.RequestAccessDto
import com.kseb.smart_car.data.requestDto.RequestNaviDto
import com.kseb.smart_car.data.responseDto.ResponseAccessDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface KakaoNaviService {
    @GET("/directions")
    suspend fun getDirections(
        @Body requestNaviDto: RequestNaviDto
    ): ResponseAccessDto
}