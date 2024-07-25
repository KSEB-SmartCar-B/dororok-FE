package com.kseb.smart_car.data.requestDto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestNaviDto (
    @SerialName("Authorization")
    val Authorization:String,
)