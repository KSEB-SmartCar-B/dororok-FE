package com.kseb.smart_car.data.repositoryImpl

import android.util.Log
import com.kseb.smart_car.data.datasource.AuthDataSource
import com.kseb.smart_car.data.requestDto.RequestAccessDto
import com.kseb.smart_car.data.requestDto.RequestSignUpDto
import com.kseb.smart_car.data.requestDto.RequestUpdateGenreDto
import com.kseb.smart_car.data.requestDto.RequestUpdateInfoDto
import com.kseb.smart_car.data.responseDto.ResponseAccessDto
import com.kseb.smart_car.data.responseDto.ResponseAllGenreDto
import com.kseb.smart_car.data.responseDto.ResponseIsSignedDto
import com.kseb.smart_car.data.responseDto.ResponseMyGenreDto
import com.kseb.smart_car.data.responseDto.ResponseMyInfoDto
import com.kseb.smart_car.data.responseDto.ResponseSignInDto
import com.kseb.smart_car.data.responseDto.ResponseSignUpDto
import com.kseb.smart_car.data.responseDto.ResponseUpdateGenreDto
import com.kseb.smart_car.data.responseDto.ResponseUpdateInfoDto
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

    override suspend fun getSignUp(info: RequestSignUpDto): Result<ResponseSignUpDto> {
        return runCatching {
            authDataSource.getSignUp(info)
        }
    }

    override suspend fun getGenreList(): Result<ResponseAllGenreDto> {
        return runCatching {
            authDataSource.getAllGenreList()
        }
    }

    override suspend fun getMyInfo(token: String): Result<ResponseMyInfoDto> {
        return runCatching {
            authDataSource.getMyInfo(token)
        }
    }

    override suspend fun getMyGenre(token: String): Result<ResponseMyGenreDto> {
        return runCatching {
            authDataSource.getMyGenre(token)
        }
    }

    override suspend fun updateInfo(
        token: String,
        info: RequestUpdateInfoDto
    ): Result<ResponseUpdateInfoDto> {
        return runCatching {
            authDataSource.updateInfo(token,info)
        }
    }

    override suspend fun updateGenre(
        token: String,
        requestUpdateGenreDto: RequestUpdateGenreDto
    ): Result<ResponseUpdateGenreDto> {
        return runCatching {
            authDataSource.updateGenre(token, requestUpdateGenreDto)
        }
    }
}