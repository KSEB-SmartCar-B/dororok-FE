package com.kseb.smart_car.data.responseDto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseFavoritePlaceDto (
    @SerialName("favoritesPlaceList")
    val favoritesPlaceList:List<FavoritesPlaceListDto>
){
    @Serializable
    data class FavoritesPlaceListDto(
        @SerialName("title")
        val title:String,
        @SerialName("address")
        val address:String,
        @SerialName("imageUrl")
        val imageUrl:String,
        @SerialName("contentId")
        val contentId:String,
    )
}