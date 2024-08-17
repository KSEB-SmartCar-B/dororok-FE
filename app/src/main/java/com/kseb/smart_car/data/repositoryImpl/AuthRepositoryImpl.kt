package com.kseb.smart_car.data.repositoryImpl

import com.kseb.smart_car.data.datasource.AuthDataSource
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
import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceNearbyDetailDto
import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceNearbyDto
import com.kseb.smart_car.data.responseDto.ResponseSaveFavoritePlaceDto
import com.kseb.smart_car.data.responseDto.ResponseSearchListDto
import com.kseb.smart_car.data.responseDto.ResponseSignInDto
import com.kseb.smart_car.data.responseDto.ResponseSignUpDto
import com.kseb.smart_car.data.responseDto.ResponseUpdateGenreDto
import com.kseb.smart_car.data.responseDto.ResponseUpdateInfoDto
import com.kseb.smart_car.domain.repository.AuthRepository
import com.spotify.protocol.types.Artist
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

    override suspend fun getSituations(): Result<ResponseSituationDto> {
        return runCatching {
            authDataSource.getSituations()
        }
    }

    override suspend fun getRecommendMusic(
        token: String,
        lat:String,
        lng:String,
        musicMode:String,
        isFirst:Int
    ): Result<ResponseRecommendMusicDto> {
        return runCatching {
            authDataSource.getRecommendMusic(token, lat,lng,musicMode,isFirst)
        }
    }

    override suspend fun getRecommendPlaceNearby(
        token: String,
        lat: String,
        lng: String,
        pageNo:Int
    ): Result<ResponseRecommendPlaceNearbyDto> {
        return runCatching {
            authDataSource.getPlacesNearby(token, lat,lng,pageNo)
        }
    }

    override suspend fun getRecommendPlaceNearbyDetail(
        token: String,
        contentId: String
    ): Result<ResponseRecommendPlaceNearbyDetailDto> {
        return runCatching {
            authDataSource.getPlacesNearbyDetail(token,contentId)
        }
    }

    override suspend fun getSearch(token: String): Result<ResponseSearchListDto> {
        return runCatching {
            authDataSource.getSearch(token)
        }
    }

    override suspend fun addSearch(token: String, addSearchDto: RequestAddSearchDto): Result<ResponseAddSearchDto> {
        return runCatching {
            authDataSource.addSearch(token,addSearchDto)
        }
    }

    override suspend fun deleteSearch(token: String, deleteSearchDto: RequestDeleteSearchDto): Result<ResponseDeleteSearchDto> {
        return runCatching {
            authDataSource.deleteSearch(token, deleteSearchDto)
        }
    }

    //개인 정보 및 선호 장르 수정
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

    override suspend fun getFavoritePlace(token: String): Result<ResponseFavoritePlaceDto> {
        return runCatching {
            authDataSource.getFavoritePlace(token)
        }
    }

    override suspend fun saveFavoritePlace(
        token: String,
        title:String,
        address:String,
        imageUrl:String,
        contentId:String
    ): Result<ResponseSaveFavoritePlaceDto> {
        return runCatching {
            authDataSource.saveFavoritePlace(token,
                ResponseFavoritePlaceDto.FavoritesPlaceListDto(title,address,imageUrl,contentId))
        }
    }

    override suspend fun deleteFavoritePlace(
        token: String,
        contentId: String
    ): Result<ResponseSaveFavoritePlaceDto> {
        return runCatching {
            authDataSource.deleteFavoritePlace(token,contentId)
        }
    }

    override suspend fun existFavoritePlace(
        token: String,
        contentId: String
    ): Result<ResponseExistFavoritePlaceDto> {
        return runCatching {
            authDataSource.existFavoritePlace(token,contentId)
        }
    }

    override suspend fun getFavoriteMusic(token: String): Result<ResponseFavoriteMusicDto> {
        return runCatching {
            authDataSource.getFavoriteMusic(token)
        }
    }

    override suspend fun addFavoriteMusic(
        token: String,
        trackId: String,
        title: String,
        artist: String,
        imageUrl: String
    ): Result<ResponseFavoriteMusicStringDto> {
        val responseFavoriteMusicDto=ResponseFavoriteMusicDto.FavoriteMusicListDto(trackId,title,artist,imageUrl)
        return runCatching {
            authDataSource.addFavoriteMusic(token,responseFavoriteMusicDto)
        }
    }

    override suspend fun deleteFavoriteMusic(
        token: String,
        trackId: String
    ): Result<ResponseFavoriteMusicStringDto> {
        return runCatching {
            authDataSource.deleteFavoriteMusic(token,trackId)
        }
    }

    override suspend fun existFavoriteMusic(
        token: String,
        trackId: String
    ): Result<ResponseExistFavoriteMusicDto> {
        return runCatching {
            authDataSource.existFavoriteMusic(token,trackId)
        }
    }
}