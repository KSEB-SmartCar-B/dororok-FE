package com.kseb.smart_car.data.datasourceImpl

import com.kseb.smart_car.data.datasource.KakaoDataSource
import com.kseb.smart_car.data.responseDto.ResponseAddressDto
import com.kseb.smart_car.data.service.KakaoMapService
import javax.inject.Inject

class KakaoDataSourceImpl @Inject constructor(
    private val kakaoMapService: KakaoMapService
):KakaoDataSource {
    override suspend fun getAddress(token: String, address: String): ResponseAddressDto =kakaoMapService.getAddress(token,address)

}