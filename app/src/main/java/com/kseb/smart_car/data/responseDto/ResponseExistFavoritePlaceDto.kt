package com.kseb.smart_car.data.responseDto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseExistFavoritePlaceDto (
    @SerialName("isExisted")
    val isExisted:Boolean
)