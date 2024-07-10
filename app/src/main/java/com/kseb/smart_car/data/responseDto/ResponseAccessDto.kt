package com.kseb.smart_car.data.responseDto

import com.kseb.smart_car.data.requestDto.RequestAccessDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseAccessDto (
    @SerialName("status")
    val status:Int,
    @SerialName("message")
    val message:String,
    @SerialName("data")
    val data: RequestAccessDto,
)