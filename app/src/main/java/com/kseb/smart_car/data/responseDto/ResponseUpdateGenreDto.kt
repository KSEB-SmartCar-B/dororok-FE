package com.kseb.smart_car.data.responseDto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseUpdateGenreDto (
    @SerialName ("result")
    val result: String
)