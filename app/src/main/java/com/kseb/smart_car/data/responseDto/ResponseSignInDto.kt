package com.kseb.smart_car.data.responseDto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseSignInDto (
    @SerialName("jwtToken")
    val jwtToken:ResponseToken,
    @SerialName("isSigned")
    val isSigned:Boolean
){
    @Serializable
    data class ResponseToken(
        @SerialName("grantType")
        val grantType:String,
        @SerialName("accessToken")
        val accessToken:String,
        @SerialName("refreshToken")
        val refreshToken:String
    )
}