package com.kseb.smart_car.data.responseDto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseDeleteSearchDto (
    @SerialName("response")
    val response:String
)