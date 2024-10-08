package com.kseb.smart_car.data.datasourceImpl

import com.kseb.smart_car.data.datasource.AuthDataSource
import com.kseb.smart_car.data.requestDto.RequestAccessDto
import com.kseb.smart_car.data.requestDto.RequestAddSearchDto
import com.kseb.smart_car.data.requestDto.RequestDeleteMusicListDto
import com.kseb.smart_car.data.requestDto.RequestDeletePlaceListDto
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
import com.kseb.smart_car.data.service.AuthService
import javax.inject.Inject

class AuthDataSourceImpl @Inject constructor(
    private val authService: AuthService
) : AuthDataSource {
    override suspend fun getAccessToken(token: RequestAccessDto): ResponseAccessDto =
        authService.getAccessToken(token)

    override suspend fun isSigned(token: String): ResponseIsSignedDto = authService.isSigned(token)

    override suspend fun getSignIn(token: String): ResponseSignInDto = authService.getSignIn(token)

    override suspend fun getSignUp(info: RequestSignUpDto): ResponseSignUpDto =
        authService.getSignUp(info)

    override suspend fun getAllGenreList(): ResponseAllGenreDto = authService.getGenreList()
    override suspend fun getSituations(): ResponseSituationDto = authService.getSituations()
    override suspend fun getRecommendMusic(
        token: String,
        lat:String,
        lng:String,
        musicMode:String,
        isFirst:Int
    ): ResponseRecommendMusicDto = authService.getRecommendMusic(token,lat,lng,musicMode,isFirst)

    override suspend fun getSearch(token: String): ResponseSearchListDto =
        authService.getSearch(token)

    override suspend fun addSearch(
        token: String,
        addSearchDto: RequestAddSearchDto
    ): ResponseAddSearchDto = authService.addSearch(token, addSearchDto)

    override suspend fun deleteSearch(
        token: String,
        deleteSearchDto: RequestDeleteSearchDto
    ): ResponseDeleteSearchDto = authService.deleteSearch(token, deleteSearchDto)

    override suspend fun getPlacesNearby(
        token: String,
        lat:String,
        lng:String,
        pageNo:Int
    ): ResponseRecommendPlaceNearbyDto = authService.getPlaceNearby(token,lat,lng,pageNo)

    override suspend fun getPlacesNearbyDetail(
        token: String,
        contentId: String
    ): ResponseRecommendPlaceNearbyDetailDto =authService.getPlaceNearbyDetail(token,contentId)

    override suspend fun getPlaces(token: String): ResponseRecommendPlaceDto = authService.getPlace(token)

    override suspend fun getPlacesDetail(
        token: String,
        contentId: String
    ): ResponseRecommendPlaceNearbyDetailDto = authService.getPlaceDetail(token,contentId)

    //개인 정보 및 선호 장르 수정
    override suspend fun getMyInfo(token: String): ResponseMyInfoDto = authService.getMyInfo(token)

    override suspend fun getMyGenre(token: String): ResponseMyGenreDto =
        authService.getMyGenre(token)

    override suspend fun updateInfo(
        token: String,
        requestUpdateInfoDto: RequestUpdateInfoDto
    ): ResponseUpdateInfoDto = authService.updateMyInfo(token, requestUpdateInfoDto)

    override suspend fun updateGenre(
        token: String,
        requestUpdateGenreDto: RequestUpdateGenreDto
    ): ResponseUpdateGenreDto = authService.updateGenre(token, requestUpdateGenreDto)

    override suspend fun getFavoritePlace(token: String): ResponseFavoritePlaceDto = authService.getFavoritePlace(token)

    override suspend fun saveFavoritePlace(
        token: String,
        responseFavoritePlaceDto: ResponseFavoritePlaceDto.FavoritesPlaceListDto
    ): ResponseSaveFavoritePlaceDto =authService.saveFavoritePlace(token,responseFavoritePlaceDto)

    override suspend fun deleteFavoritePlace(
        token: String,
        contentId: String
    ): ResponseSaveFavoritePlaceDto =authService.deleteFavoritePlace(token,contentId)

    override suspend fun deleteFavoritePlaceList(
        token: String,
        requestDeletePlaceListDto: RequestDeletePlaceListDto
    ): ResponseSaveFavoritePlaceDto = authService.deletePlaceList(token,requestDeletePlaceListDto)

    override suspend fun existFavoritePlace(
        token: String,
        contentId: String
    ): ResponseExistFavoritePlaceDto = authService.existFavoritePlace(token,contentId)

    override suspend fun getFavoriteMusic(token: String): ResponseFavoriteMusicDto =authService.getFavoriteMusic(token)

    override suspend fun addFavoriteMusic(
        token: String,
        responseFavoriteMusicDto: ResponseFavoriteMusicDto.FavoriteMusicListDto
    ): ResponseFavoriteMusicStringDto =authService.addFavoriteMusic(token,responseFavoriteMusicDto)

    override suspend fun deleteFavoriteMusic(
        token: String,
        trackId: String
    ): ResponseFavoriteMusicStringDto =authService.deleteFavoritesMusic(token,trackId)

    override suspend fun deleteFavoriteMusicList(
        token: String,
        requestDeleteMusicListDto: RequestDeleteMusicListDto
    ): ResponseFavoriteMusicStringDto = authService.deleteMusicList(token,requestDeleteMusicListDto)

    override suspend fun existFavoriteMusic(
        token: String,
        trackId: String
    ): ResponseExistFavoriteMusicDto = authService.existFavoriteMusic(token,trackId)
}