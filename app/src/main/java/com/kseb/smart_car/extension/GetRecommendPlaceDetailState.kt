package com.kseb.smart_car.extension

import com.kseb.smart_car.data.responseDto.ResponseMyGenreDto
import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceNearbyDetailDto

sealed class GetRecommendPlaceDetailState {
    data object Loading:GetRecommendPlaceDetailState()
    data class Success(val detailDto: ResponseRecommendPlaceNearbyDetailDto):GetRecommendPlaceDetailState()
    data class Error(val message:String):GetRecommendPlaceDetailState()
}