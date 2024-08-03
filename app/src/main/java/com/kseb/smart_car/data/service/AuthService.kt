package com.kseb.smart_car.data.service

import com.kseb.smart_car.data.requestDto.RequestAccessDto
import com.kseb.smart_car.data.requestDto.RequestAddSearchDto
import com.kseb.smart_car.data.requestDto.RequestDeleteSearchDto
import com.kseb.smart_car.data.requestDto.RequestSignUpDto
import com.kseb.smart_car.data.requestDto.RequestUpdateGenreDto
import com.kseb.smart_car.data.requestDto.RequestUpdateInfoDto
import com.kseb.smart_car.data.responseDto.ResponseAccessDto
import com.kseb.smart_car.data.responseDto.ResponseAddSearchDto
import com.kseb.smart_car.data.responseDto.ResponseAllGenreDto
import com.kseb.smart_car.data.responseDto.ResponseDeleteSearchDto
import com.kseb.smart_car.data.responseDto.ResponseIsSignedDto
import com.kseb.smart_car.data.responseDto.ResponseMyGenreDto
import com.kseb.smart_car.data.responseDto.ResponseMyInfoDto
import com.kseb.smart_car.data.responseDto.ResponseSearchListDto
import com.kseb.smart_car.data.responseDto.ResponseSignInDto
import com.kseb.smart_car.data.responseDto.ResponseSignUpDto
import com.kseb.smart_car.data.responseDto.ResponseUpdateGenreDto
import com.kseb.smart_car.data.responseDto.ResponseUpdateInfoDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

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

    @GET("genre/name-list")
    suspend fun getGenreList():ResponseAllGenreDto

    //검색창
    @GET("search")
    suspend fun getSearch(
        @Header("Authorization") token:String
    ):ResponseSearchListDto

    @POST("search")
    suspend fun addSearch(
        @Header("Authorization") token:String,
        @Body addSearchDto: RequestAddSearchDto
    ):ResponseAddSearchDto

    @POST("search/delete")
    suspend fun deleteSearch(
        @Header("Authorization") token:String,
        @Body deleteSearchDto: RequestDeleteSearchDto
    ):ResponseDeleteSearchDto


    //개인 정보 및 선호 장르 수정
    @GET("members/info")
    suspend fun getMyInfo(
        @Header("Authorization") token:String,
    ):ResponseMyInfoDto

    @GET("members/favorite-genre")
    suspend fun getMyGenre(
        @Header("Authorization") token:String,
    ):ResponseMyGenreDto

    @PATCH("members/info")
    suspend fun updateMyInfo(
        @Header("Authorization") token:String,
        @Body requestUpdateInfoDto: RequestUpdateInfoDto
    ):ResponseUpdateInfoDto

    @PATCH("members/favorite-genre")
    suspend fun updateGenre(
        @Header("Authorization") token: String,
        @Body requestUpdateGenreDto: RequestUpdateGenreDto
    ): ResponseUpdateGenreDto
}