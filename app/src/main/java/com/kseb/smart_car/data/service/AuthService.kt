package com.kseb.smart_car.data.service

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
import dagger.hilt.internal.GeneratedEntryPoint
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Url

interface AuthService {
    @POST("api/users")
    suspend fun getAccessToken(
        @Body requestAccessDto: RequestAccessDto
    ): ResponseAccessDto

    @POST("members/isSigned")
    suspend fun isSigned(
        @Body kakaoAccessToken: String
    ): ResponseIsSignedDto

    @POST("members/sign-in")
    suspend fun getSignIn(
        @Body kakaoAccessToken:String
    ): ResponseSignInDto

    @POST("members/sign-up")
    suspend fun getSignUp(
        @Body requestSignUpDto: RequestSignUpDto
    ):ResponseSignUpDto

    @GET("members/info")
    suspend fun getMyInfo(
        @Header("Authorization") token:String,
    ):ResponseMyInfoDto

    @GET("members/favorite-genre")
    suspend fun getMyGenre(
        @Header("Authorization") token:String,
    ):ResponseMyGenreDto

    @PATCH("members/favorite-genre")
    suspend fun updateGenre(
        @Header("Authorization") token: String,
        @Body requestUpdateGenreDto: RequestUpdateGenreDto
    ): ResponseUpdateGenreDto
}