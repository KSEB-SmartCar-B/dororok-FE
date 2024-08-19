package com.kseb.smart_car.extension

import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceDto


sealed class RecommendPlaceState {
    data object Loading:RecommendPlaceState()
    data class Success(val placeDto: ResponseRecommendPlaceDto):RecommendPlaceState()
    data class Error(val message:String):RecommendPlaceState()
}