package com.kseb.smart_car.data.responseDto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ResponseRecommendPlaceNearbyDto (
    @SerialName("places")
    val places:List<PlaceList>,
    @SerialName("pageNumbers")
    val pageNumbers:Int
):Parcelable{
    @Parcelize
    @Serializable
    data class PlaceList(
        @SerialName("contentId")
        val contentId:String,
        @SerialName("address")
        val address:String,
        @SerialName("region1depthName")
        val region1depthName:String,
        @SerialName("region2depthName")
        val region2depthName:String,
        @SerialName("region3depthName")
        val region3depthName:String,
        @SerialName("imageUrl")
        val imageUrl:String,
        @SerialName("title")
        val title:String,
    ):Parcelable
}