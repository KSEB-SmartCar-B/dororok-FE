package com.kseb.smart_car.presentation.join

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Join2 (
    val genre: String,
    var isSelected: Boolean = false,
): Parcelable