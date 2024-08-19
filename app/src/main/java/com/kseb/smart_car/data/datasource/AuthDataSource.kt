package com.kseb.smart_car.data.datasource

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
import com.kseb.smart_car.data.responseDto.ResponseExistFavoriteMusicDto
import com.kseb.smart_car.data.responseDto.ResponseExistFavoritePlaceDto
import com.kseb.smart_car.data.responseDto.ResponseFavoriteMusicDto
import com.kseb.smart_car.data.responseDto.ResponseFavoriteMusicStringDto
import com.kseb.smart_car.data.responseDto.ResponseFavoritePlaceDto
import com.kseb.smart_car.data.responseDto.ResponseRecommendMusicDto
import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceDto
import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceNearbyDetailDto
import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceNearbyDto
import com.kseb.smart_car.data.responseDto.ResponseSaveFavoritePlaceDto
import com.kseb.smart_car.data.responseDto.ResponseSearchListDto
import com.kseb.smart_car.data.responseDto.ResponseSignInDto
import com.kseb.smart_car.data.responseDto.ResponseSignUpDto
import com.kseb.smart_car.data.responseDto.ResponseUpdateGenreDto
import com.kseb.smart_car.data.responseDto.ResponseUpdateInfoDto
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

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

    suspend fun getAllGenreList():ResponseAllGenreDto

    suspend fun getSituations():ResponseSituationDto

    suspend fun getRecommendMusic(
        token:String,
        lat:String,
        lng:String,
        musicMode:String,
        isFirst:Int
    ): ResponseRecommendMusicDto

    suspend fun getSearch(
        token:String
    ):ResponseSearchListDto

    suspend fun addSearch(
        token:String,
        requestAddSearchDto: RequestAddSearchDto
    ):ResponseAddSearchDto

    suspend fun deleteSearch(
        token:String,
        deleteSearchDto: RequestDeleteSearchDto
    ):ResponseDeleteSearchDto

    //내 주변 여행지
    suspend fun getPlacesNearby(
        token:String,
        lat:String,
        lng:String,
        pageNo:Int
    ):ResponseRecommendPlaceNearbyDto

    suspend fun getPlacesNearbyDetail(
        token:String,
        contentId:String
    ):ResponseRecommendPlaceNearbyDetailDto

    suspend fun getPlaces(
        token:String,
    ):ResponseRecommendPlaceDto

    suspend fun getPlacesDetail(
        token:String,
        contentId: String
    ):ResponseRecommendPlaceNearbyDetailDto

    //개인 정보 및 선호 장르 수정
    suspend fun getMyInfo(
        token:String
    ):ResponseMyInfoDto

    suspend fun getMyGenre(
        token:String
    ): ResponseMyGenreDto

    suspend fun updateInfo(
        token:String,
        requestUpdateInfoDto: RequestUpdateInfoDto
    ):ResponseUpdateInfoDto

    suspend fun updateGenre(
        token: String,
        requestUpdateGenreDto: RequestUpdateGenreDto
    ): ResponseUpdateGenreDto

    //저장된 장소
    suspend fun getFavoritePlace(
        token: String,
    ):ResponseFavoritePlaceDto

    suspend fun saveFavoritePlace(
        token: String,
        responseFavoritePlaceDto: ResponseFavoritePlaceDto.FavoritesPlaceListDto
    ):ResponseSaveFavoritePlaceDto

    suspend fun deleteFavoritePlace(
        token:String,
        contentId:String
    ):ResponseSaveFavoritePlaceDto

    suspend fun existFavoritePlace(
        token:String,
        contentId: String
    ):ResponseExistFavoritePlaceDto

    //저장된 음악
    suspend fun getFavoriteMusic(
        token:String,
    ):ResponseFavoriteMusicDto

    suspend fun addFavoriteMusic(
        token:String,
        responseFavoriteMusicDto: ResponseFavoriteMusicDto.FavoriteMusicListDto
    ):ResponseFavoriteMusicStringDto

    suspend fun deleteFavoriteMusic(
        token:String,
        trackId:String,
    ):ResponseFavoriteMusicStringDto

    suspend fun existFavoriteMusic(
        token:String,
        trackId:String
    ):ResponseExistFavoriteMusicDto
}