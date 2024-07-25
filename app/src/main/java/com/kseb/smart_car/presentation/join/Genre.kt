package com.kseb.smart_car.presentation.join

import android.graphics.drawable.Drawable
import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize

data class Genre(
    val name: String,
    @DrawableRes val photo: Int?
):Parcelable