package com.kseb.smart_car.data.responseDto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseFavoriteMusicStringDto (
    @SerialName("response")
    val response:String
)