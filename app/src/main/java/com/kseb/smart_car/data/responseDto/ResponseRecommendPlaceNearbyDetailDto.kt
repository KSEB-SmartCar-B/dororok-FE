package com.kseb.smart_car.data.responseDto

import com.kseb.smart_car.presentation.main.place.placeDetail.PlaceDetailFragment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseRecommendPlaceNearbyDetailDto (
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

