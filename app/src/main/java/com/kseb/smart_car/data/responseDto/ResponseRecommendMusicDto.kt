package com.kseb.smart_car.data.responseDto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseRecommendMusicDto (
    @SerialName("lists")
    val lists:List<RecommendMusicList>
){
    @Serializable
    data class RecommendMusicList(
        @SerialName("title")
        val title:String,
        @SerialName("artist")
        val artist:String,
        @SerialName("track_id")
        val trackId:String,
        @SerialName("album_image")
        val albumImage:String
    )
}