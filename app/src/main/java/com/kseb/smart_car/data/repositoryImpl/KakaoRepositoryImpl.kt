package com.kseb.smart_car.data.repositoryImpl

import android.util.Log
import com.kseb.smart_car.data.datasource.KakaoDataSource
import com.kseb.smart_car.data.responseDto.ResponseAddressDto
import com.kseb.smart_car.domain.repository.KakaoRepository
import javax.inject.Inject

class KakaoRepositoryImpl @Inject constructor(
    private val kakaoDataSource: KakaoDataSource
):KakaoRepository {
    override suspend fun getAddress(token: String, address: String): Result<ResponseAddressDto> {
        return runCatching {
            kakaoDataSource.getAddress(token,address)
        }.onFailure {
            Log.e("kakaoRepositoryImpl", "kakaoRepositoryImpl 실패", it)
        }
    }


}