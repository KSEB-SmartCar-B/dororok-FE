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
import com.kseb.smart_car.data.responseDto.ResponseSituationDto
import com.kseb.smart_car.data.responseDto.ResponseMyGenreDto
import com.kseb.smart_car.data.responseDto.ResponseMyInfoDto
import com.kseb.smart_car.data.requestDto.RequestRecommendMusicDto
import com.kseb.smart_car.data.requestDto.RequestRecommendPlaceDto
import com.kseb.smart_car.data.responseDto.ResponseFavoritePlaceDto
import com.kseb.smart_car.data.responseDto.ResponseRecommendMusicDto
import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceNearbyDto
import com.kseb.smart_car.data.responseDto.ResponseSaveFavoritePlaceDto
import com.kseb.smart_car.data.responseDto.ResponseSearchListDto
import com.kseb.smart_car.data.responseDto.ResponseSignInDto
import com.kseb.smart_car.data.responseDto.ResponseSignUpDto
import com.kseb.smart_car.data.responseDto.ResponseUpdateGenreDto
import com.kseb.smart_car.data.responseDto.ResponseUpdateInfoDto
import retrofit2.http.Body
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

    @GET("/musicmode")
    suspend fun getSituations():ResponseSituationDto

    @GET("/recommendation/music")
    suspend fun getRecommendMusic(
        @Header("Authorization") token:String,
        @Query("query") requestRecommendMusicDto: RequestRecommendMusicDto
    ): ResponseRecommendMusicDto

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

    //내 주변 여행지
    @GET("/recommendation/places/nearby")
    suspend fun getPlaceNearby(
        @Header("Authorization") token:String,
        @Query("lat") lat:String,
        @Query("lng") lng:String,
        @Query("pageNo") pageNo:Int,
    ):ResponseRecommendPlaceNearbyDto


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

    //저장된 장소
    @GET("/favorites/place")
    suspend fun getFavoritePlace(
        @Header("Authorization") token: String,
    ):ResponseFavoritePlaceDto

    //장소 저장
    @POST("/favorites/place")
    suspend fun saveFavoritePlace(
        @Header("Authorization") token: String,
        @Body responseFavoritePlaceDto: ResponseFavoritePlaceDto.FavoritesPlaceListDto
    ):ResponseSaveFavoritePlaceDto

    @POST("/favorites/place/delete")
    suspend fun deleteFavoritePlace(
        @Header("Authorization") token: String,
        @Body contentId:String
    ):ResponseSaveFavoritePlaceDto


}