package com.kseb.smart_car.data.requestDto
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class RequestSignUpDto (
    @SerialName("kakaoAccessToken")
    val kakaoAccessToken:String,
    @SerialName("nickname")
    val nickname:String,
    @SerialName("gender")
    val gender:String,
    @SerialName("birthday")
    @Contextual
    val birthday:LocalDateTime,
    @SerialName("privacyAgreement")
    val privacyAgreement:Boolean,
    @SerialName("locationInfoAgreement")
    val locationInfoAgreement:Boolean,
)