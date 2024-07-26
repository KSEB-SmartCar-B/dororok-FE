package com.kseb.smart_car.data.requestDto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class RequestUpdateInfoDto (
    @SerialName("nickname")
    val nickname:String,
    @Serializable(with = LocalDateSerializer::class)
    @SerialName("birthday")
    val birthday:LocalDate,
    @SerialName("gender")
    val gender:String
)