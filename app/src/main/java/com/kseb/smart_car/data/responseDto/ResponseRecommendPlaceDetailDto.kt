package com.kseb.smart_car.data.responseDto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseRecommendPlaceDetailDto (
    @SerialName("placeDetail")
    val placeDetail:PlaceDetailDto,
    @SerialName("placeList")
    val placeList:List<ResponseRecommendPlaceNearbyDto.PlaceList>
){
    @Serializable
    data class PlaceDetailDto(
        @SerialName("contentId")
        val contentId:String,
        @SerialName("address")
        val address:String,
        @SerialName("imageUrl")
        val imageUrl:String,
        @SerialName("title")
        val title:String,
    )
}
