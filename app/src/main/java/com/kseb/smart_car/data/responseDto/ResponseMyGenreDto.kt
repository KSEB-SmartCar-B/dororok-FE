package com.kseb.smart_car.data.responseDto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseMyGenreDto (
    @SerialName("favoriteGenres")
    val favoriteGenres: List<Name>
) {
    @Serializable
    data class Name(
        @SerialName("name")
        val name: String
    )
}