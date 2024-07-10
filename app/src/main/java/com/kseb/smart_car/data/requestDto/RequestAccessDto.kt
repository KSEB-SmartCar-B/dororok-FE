package com.kseb.smart_car.data.requestDto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestAccessDto (
    @SerialName("accessToken")
    val kakaoToken:String
)