package com.kseb.smart_car.data.responseDto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseRecommendPlaceDto (
    @SerialName("places")
    val places:List<ResponseRecommendPlaceNearbyDto.PlaceList>,
    @SerialName("pageNumbers")
    val pageNumbers:Int?
)