package com.kseb.smart_car.data.service

import com.kseb.smart_car.data.requestDto.RequestNaviDto
import com.kseb.smart_car.data.responseDto.ResponseAccessDto
import com.kseb.smart_car.data.responseDto.ResponseAddressDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoMapService {
    @GET("local/search/keyword.json")
    suspend fun getAddress(
        @Header("Authorization") token :String,
        @Query("query") address: String
    ): ResponseAddressDto
}