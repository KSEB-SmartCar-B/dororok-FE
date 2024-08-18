package com.kseb.smart_car.data.requestDto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestDeletePlaceListDto (
    @SerialName("contentIds")
    val contentIds:List<String>
)