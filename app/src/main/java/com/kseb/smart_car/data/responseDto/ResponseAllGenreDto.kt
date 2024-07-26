package com.kseb.smart_car.data.responseDto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseAllGenreDto (
    @SerialName("names")
    val names:List<GenreDto>
){
    @Serializable
    data class GenreDto(
        @SerialName("name")
        val name:String,
        @SerialName("imageUrl")
        val imageUrl:String
    )
}