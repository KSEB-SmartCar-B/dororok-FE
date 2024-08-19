package com.kseb.smart_car.data.requestDto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.Serial

@Serializable
data class RequestDeleteMusicListDto (
    @SerialName("trackIds")
    val trackIds:List<String>
)