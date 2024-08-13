package com.kseb.smart_car.data.responseDto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseSituationDto (
    @SerialName("musicModeList")
    val musicModeList:List<SituationList>
){
    @Serializable
    data class SituationList(
        @SerialName("name")
        val name:String,
        @SerialName("imageUrl")
        val imageUrl:String
    )
}