package com.kseb.smart_car.data.responseDto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseFavoriteMusicDto (
   @SerialName("favoritesMusicList")
    val favoritesMusicList:List<FavoriteMusicListDto>
){
    @Serializable
    data class FavoriteMusicListDto(
        @SerialName("trackId")
        val trackId:String,
        @SerialName("title")
        val title:String,
        @SerialName("artist")
        val artist:String,
        @SerialName("imageUrl")
        val imageUrl:String
    )
}