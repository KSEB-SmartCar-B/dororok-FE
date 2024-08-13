package com.kseb.smart_car.extension

import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceNearbyDto


sealed class RecommendPlaceNearbyState {
    data object Loading:RecommendPlaceNearbyState()
    data class Success(val placeNearbyDto: ResponseRecommendPlaceNearbyDto):RecommendPlaceNearbyState()
    data class Error(val message:String):RecommendPlaceNearbyState()
}