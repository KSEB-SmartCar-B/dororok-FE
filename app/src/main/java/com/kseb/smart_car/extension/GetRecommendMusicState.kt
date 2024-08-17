package com.kseb.smart_car.extension

import com.kseb.smart_car.data.responseDto.ResponseFavoritePlaceDto
import com.kseb.smart_car.data.responseDto.ResponseRecommendMusicDto

sealed class GetRecommendMusicState {
    data object Loading:GetRecommendMusicState()
    data class Success(val recommendMusicList:ResponseRecommendMusicDto):GetRecommendMusicState()
    data class Error(val message:String):GetRecommendMusicState()
}