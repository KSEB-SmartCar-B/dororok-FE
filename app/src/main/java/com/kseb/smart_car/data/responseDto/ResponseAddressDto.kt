package com.kseb.smart_car.data.responseDto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseAddressDto (
    @SerialName("meta")
    val meta: Meta,
    @SerialName("documents")
    val documents: List<Document>
) {
    @Serializable
    data class Meta(
        @SerialName("same_name")
        val sameName: SameName,
        @SerialName("total_count")
        val totalCount: Int,
        @SerialName("pageable_count")
        val pageableCount: Int,
        @SerialName("is_end")
        val isEnd: Boolean
    ) {
        @Serializable
        data class SameName(
            @SerialName("region")
            val region: List<String>,
            @SerialName("keyword")
            val keyword: String,
            @SerialName("selected_region")
            val selectedRegion: String
        )
    }

    @Serializable
    data class Document(
        @SerialName("place_name")
        val placeName: String,
        @SerialName("distance")
        val distance: String,
        @SerialName("place_url")
        val placeUrl: String,
        @SerialName("category_name")
        val categoryName: String,
        @SerialName("address_name")
        val addressName: String,
        @SerialName("road_address_name")
        val roadAddressName: String,
        @SerialName("id")
        val id: String,
        @SerialName("phone")
        val phone: String,
        @SerialName("category_group_code")
        val categoryGroupCode: String?,
        @SerialName("category_group_name")
        val categoryGroupName: String?,
        @SerialName("x")
        val x: String,
        @SerialName("y")
        val y: String
    )
}