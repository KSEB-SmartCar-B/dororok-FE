package com.kseb.smart_car.data.repositoryImpl

import com.kseb.smart_car.data.datasource.AuthDataSource
import com.kseb.smart_car.data.requestDto.RequestAccessDto
import com.kseb.smart_car.data.responseDto.ResponseAccessDto
import com.kseb.smart_car.data.responseDto.ResponseIsSignedDto
import com.kseb.smart_car.data.responseDto.ResponseSignInDto
import com.kseb.smart_car.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource
):AuthRepository {
    override suspend fun getAccessToken(token: String): Result<ResponseAccessDto> {
        return runCatching{
            authDataSource.getAccessToken(RequestAccessDto(token))
        }
    }

    override suspend fun isSigned(token: String): Result<ResponseIsSignedDto> {
        return runCatching {
            authDataSource.isSigned(token)
        }
    }
    override suspend fun getSignIn(token: String): Result<ResponseSignInDto> {
        return runCatching {
            authDataSource.getSignIn(token)
        }
    }
}