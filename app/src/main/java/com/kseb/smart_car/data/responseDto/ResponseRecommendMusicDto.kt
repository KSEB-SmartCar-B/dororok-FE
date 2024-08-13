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
        @SerialName("TRACK_URI")
        val trackUri:String,
        @SerialName("PLAY_TIME")
        val playTime:Long,
        @SerialName("TRACK_IMAGE")
        val trackImage:String,
    )
}