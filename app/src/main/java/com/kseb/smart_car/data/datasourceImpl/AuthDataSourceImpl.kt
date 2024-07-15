package com.kseb.smart_car.data.datasourceImpl

import com.kseb.smart_car.data.datasource.AuthDataSource
import com.kseb.smart_car.data.requestDto.RequestAccessDto
import com.kseb.smart_car.data.responseDto.ResponseAccessDto
import com.kseb.smart_car.data.responseDto.ResponseIsSignedDto
import com.kseb.smart_car.data.responseDto.ResponseSignInDto
import com.kseb.smart_car.data.service.AuthService
import javax.inject.Inject

class AuthDataSourceImpl @Inject constructor(
    private val authService: AuthService
): AuthDataSource {
    override suspend fun getAccessToken(token: RequestAccessDto): ResponseAccessDto = authService.getAccessToken(token)

    override suspend fun isSigned(token: String):ResponseIsSignedDto = authService.isSigned(token)

    override suspend fun getSignIn(token: String): ResponseSignInDto = authService.getSignIn(token)
}