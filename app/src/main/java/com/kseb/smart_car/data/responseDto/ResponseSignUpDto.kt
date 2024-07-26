package com.kseb.smart_car.data.responseDto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class  ResponseSignUpDto (
    @SerialName("grantType")
    val grantType:String,
    @SerialName("accessToken")
    val accessToken:String,
    @SerialName("refreshToken")
    val refreshToken:String
)