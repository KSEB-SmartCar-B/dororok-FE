package com.kseb.smart_car.data.requestDto

import com.kseb.smart_car.data.responseDto.ResponseMyGenreDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestUpdateGenreDto (
    @SerialName("favoriteGenres")
    val favoriteGenres: List<ResponseMyGenreDto.Name>,
)