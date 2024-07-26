package com.kseb.smart_car.data.datasourceImpl

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
import com.kseb.smart_car.data.service.AuthService
import javax.inject.Inject

class AuthDataSourceImpl @Inject constructor(
    private val authService: AuthService
): AuthDataSource {
    override suspend fun getAccessToken(token: RequestAccessDto): ResponseAccessDto =
        authService.getAccessToken(token)

    override suspend fun isSigned(token: String): ResponseIsSignedDto = authService.isSigned(token)

    override suspend fun getSignIn(token: String): ResponseSignInDto = authService.getSignIn(token)

    override suspend fun getSignUp(info: RequestSignUpDto): ResponseSignUpDto =
        authService.getSignUp(info)

    override suspend fun getAllGenreList(): ResponseAllGenreDto  = authService.getGenreList()

    override suspend fun getMyInfo(token: String): ResponseMyInfoDto = authService.getMyInfo(token)

    override suspend fun getMyGenre(token: String): ResponseMyGenreDto = authService.getMyGenre(token)

    override suspend fun updateInfo(
        token: String,
        requestUpdateInfoDto: RequestUpdateInfoDto
    ): ResponseUpdateInfoDto = authService.updateMyInfo(token,requestUpdateInfoDto)

    override suspend fun updateGenre(
        token: String,
        requestUpdateGenreDto: RequestUpdateGenreDto
    ): ResponseUpdateGenreDto = authService.updateGenre(token, requestUpdateGenreDto)


}