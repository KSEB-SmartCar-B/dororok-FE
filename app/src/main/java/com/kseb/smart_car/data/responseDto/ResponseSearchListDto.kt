package com.kseb.smart_car.data.responseDto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseSearchListDto (
    @SerialName("searchLogs")
    val searchLogs:List<String>
)