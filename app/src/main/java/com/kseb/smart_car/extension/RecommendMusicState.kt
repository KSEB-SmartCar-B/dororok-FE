package com.kseb.smart_car.extension

import com.kseb.smart_car.data.requestDto.RequestRecommendMusicDto

sealed class RecommendMusicState {
    data object Loading:RecommendMusicState()
    data class Success(val situationDto: RequestRecommendMusicDto):RecommendMusicState()
    data class Error(val message:String):RecommendMusicState()
}