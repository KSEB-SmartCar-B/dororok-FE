package com.kseb.smart_car.data.responseDto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseMusicDto (
    @SerialName("lists")
    val lists:List<MusicListDto>
){
    @Serializable
    data class MusicListDto(
        @SerialName("TRACK_URI")
        val trackUri:String,
        @SerialName("PLAY_TIME")
        val playTime:Int,
        @SerialName("TRACK_IMAGE")
        val trackImage:String,
    )
}