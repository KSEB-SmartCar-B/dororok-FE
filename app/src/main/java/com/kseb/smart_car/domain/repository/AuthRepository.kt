package com.kseb.smart_car.domain.repository

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
import com.kseb.smart_car.data.responseDto.ResponseExistFavoriteMusicDto
import com.kseb.smart_car.data.responseDto.ResponseExistFavoritePlaceDto
import com.kseb.smart_car.data.responseDto.ResponseFavoriteMusicDto
import com.kseb.smart_car.data.responseDto.ResponseFavoriteMusicStringDto
import com.kseb.smart_car.data.responseDto.ResponseFavoritePlaceDto
import com.kseb.smart_car.data.responseDto.ResponseRecommendMusicDto
import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceDetailDto
import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceDto
import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceNearbyDetailDto
import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceNearbyDto
import com.kseb.smart_car.data.responseDto.ResponseSaveFavoritePlaceDto
import com.kseb.smart_car.data.responseDto.ResponseSearchListDto
import com.kseb.smart_car.data.responseDto.ResponseSignInDto
import com.kseb.smart_car.data.responseDto.ResponseSignUpDto
import com.kseb.smart_car.data.responseDto.ResponseUpdateGenreDto
import com.kseb.smart_car.data.responseDto.ResponseUpdateInfoDto

interface AuthRepository {
    suspend fun getAccessToken(
        token:String,
    ):Result<ResponseAccessDto>

    suspend fun isSigned(
        token:String
    ):Result<ResponseIsSignedDto>

    suspend fun getSignIn(
        token:String
    ):Result<ResponseSignInDto>

    suspend fun getSignUp(
        info:RequestSignUpDto
    ):Result<ResponseSignUpDto>

    suspend fun getGenreList():Result<ResponseAllGenreDto>

    suspend fun getSituations():Result<ResponseSituationDto>

    suspend fun getRecommendMusic(
        token:String,
        lat:String,
        lng:String,
        musicMode:String,
        isFirst:Int
    ):Result<ResponseRecommendMusicDto>

    suspend fun getRecommendPlaceNearby(
        token:String,
        lat:String,
        lng:String,
        pageNo:Int
    ):Result<ResponseRecommendPlaceNearbyDto>

    suspend fun getRecommendPlaceNearbyDetail(
        token:String,
        contentId: String
    ):Result<ResponseRecommendPlaceNearbyDetailDto>

    suspend fun getRecommendPlace(
        token:String,
    ):Result<ResponseRecommendPlaceDto>

    suspend fun getRecommendPlaceDetail(
        token:String,
        contentId: String
    ):Result<ResponseRecommendPlaceDetailDto>

    suspend fun getSearch(
        token:String
    ):Result<ResponseSearchListDto>

    suspend fun addSearch(
        token:String,
        addSearchDto: RequestAddSearchDto
    ):Result<ResponseAddSearchDto>

    suspend fun deleteSearch(
        token:String,
        deleteSearchDto: RequestDeleteSearchDto
    ):Result<ResponseDeleteSearchDto>

    //개인 정보 및 선호 장르 수정
    suspend fun getMyInfo(
        token:String
    ):Result<ResponseMyInfoDto>

    suspend fun getMyGenre(
        token:String
    ):Result<ResponseMyGenreDto>

    suspend fun updateInfo(
        token:String,
        info: RequestUpdateInfoDto
    ):Result<ResponseUpdateInfoDto>

    suspend fun updateGenre(
        token: String,
        requestUpdateGenreDto: RequestUpdateGenreDto
    ):Result<ResponseUpdateGenreDto>

    //저장된 장소
    suspend fun getFavoritePlace(
        token:String
    ):Result<ResponseFavoritePlaceDto>

    //장소 저장
    suspend fun saveFavoritePlace(
        token:String,
        title:String,
        address:String,
        imageUrl:String,
        contentId:String
    ):Result<ResponseSaveFavoritePlaceDto>

    suspend fun deleteFavoritePlace(
        token:String,
        contentId:String
    ):Result<ResponseSaveFavoritePlaceDto>

    suspend fun deleteFavoritePlaceList(
        token:String,
        contentIds:List<String>
    ):Result<ResponseSaveFavoritePlaceDto>

    suspend fun existFavoritePlace(
        token:String,
        contentId: String
    ):Result<ResponseExistFavoritePlaceDto>

    //저장된 음악
    suspend fun getFavoriteMusic(
        token:String
    ):Result<ResponseFavoriteMusicDto>

    suspend fun addFavoriteMusic(
        token:String,
        trackId: String,
        title:String,
        artist:String,
        imageUrl: String
    ):Result<ResponseFavoriteMusicStringDto>

    suspend fun deleteFavoriteMusic(
        token:String,
        trackId:String,
    ):Result<ResponseFavoriteMusicStringDto>

    suspend fun deleteFavoriteMusicList(
        token:String,
        trackIds:List<String>,
    ):Result<ResponseFavoriteMusicStringDto>

    suspend fun existFavoriteMusic(
        token:String,
        trackId: String
    ):Result<ResponseExistFavoriteMusicDto>
}