package com.kseb.smart_car.data.requestDto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestRecommendPlaceDto (
    @SerialName("lat")
    val lat:String,
    @SerialName("lng")
    val lng:String
)