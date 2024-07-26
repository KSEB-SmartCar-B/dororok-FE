package com.kseb.smart_car.extension

import com.kseb.smart_car.data.responseDto.ResponseMyGenreDto
import com.kseb.smart_car.data.responseDto.ResponseMyInfoDto

sealed class GenreState {
    data object Loading:GenreState()
    data class Success(val genreDto: ResponseMyGenreDto):GenreState()
    data class Error(val message:String):GenreState()
}