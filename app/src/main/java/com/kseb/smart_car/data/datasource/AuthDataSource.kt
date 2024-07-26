package com.kseb.smart_car.data.datasource

import com.kseb.smart_car.data.requestDto.RequestAccessDto
import com.kseb.smart_car.data.requestDto.RequestSignUpDto
import com.kseb.smart_car.data.requestDto.RequestUpdateGenreDto
import com.kseb.smart_car.data.responseDto.ResponseAccessDto
import com.kseb.smart_car.data.responseDto.ResponseIsSignedDto
import com.kseb.smart_car.data.responseDto.ResponseMyGenreDto
import com.kseb.smart_car.data.responseDto.ResponseMyInfoDto
import com.kseb.smart_car.data.responseDto.ResponseSignInDto
import com.kseb.smart_car.data.responseDto.ResponseSignUpDto
import com.kseb.smart_car.data.responseDto.ResponseUpdateGenreDto
import retrofit2.http.Body
import retrofit2.http.Header

interface AuthDataSource {
    suspend fun getAccessToken(
        token: RequestAccessDto
    ): ResponseAccessDto

    suspend fun isSigned(
        token:String
    ):ResponseIsSignedDto

    suspend fun getSignIn(
        token:String
    ): ResponseSignInDto

    suspend fun getSignUp(
        info:RequestSignUpDto
    ):ResponseSignUpDto

    suspend fun getMyInfo(
        @Header("Authorization") token:String
    ):ResponseMyInfoDto

    suspend fun getMyGenre(
        @Header("Authorization") token:String
    ): ResponseMyGenreDto

    suspend fun updateGenre(
        @Header("Authorization") token: String,
        requestUpdateGenreDto: RequestUpdateGenreDto
    ): ResponseUpdateGenreDto
}