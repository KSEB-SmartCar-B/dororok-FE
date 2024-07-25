package com.kseb.smart_car.data.responseDto

import com.kseb.smart_car.data.requestDto.LocalDateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class ResponseMyInfoDto (
    @SerialName("nickname")
    val nickname:String,
    @Serializable(with = LocalDateSerializer::class)
    @SerialName("birthday")
    val birthday: LocalDate,
    @SerialName("gender")
    val gender:String,
    @SerialName("profileImgUrl")
    val profile:String,
)