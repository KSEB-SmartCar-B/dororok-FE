package com.kseb.smart_car.data.datasourceImpl

import com.kseb.smart_car.data.datasource.AuthDataSource
import com.kseb.smart_car.data.requestDto.RequestAccessDto
import com.kseb.smart_car.data.responseDto.ResponseAccessDto
import com.kseb.smart_car.data.service.AuthService
import javax.inject.Inject

class AuthDataSourceImpl @Inject constructor(
    private val authService: AuthService
): AuthDataSource {
    override suspend fun getAccessToken(token: RequestAccessDto): ResponseAccessDto = authService.getAccessToken(token)

}