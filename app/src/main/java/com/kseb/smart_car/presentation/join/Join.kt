package com.kseb.smart_car.presentation.join

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Join (
    val gender: String,
    val nick: String,
    val birthYear: String,
    val birthMonth: String,
    val birthDay: String,
    val genre: List<String>,
): Parcelable