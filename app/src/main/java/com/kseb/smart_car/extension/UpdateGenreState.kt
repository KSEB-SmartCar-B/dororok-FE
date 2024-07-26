package com.kseb.smart_car.extension

import com.kseb.smart_car.data.responseDto.ResponseMyGenreDto
import com.kseb.smart_car.data.responseDto.ResponseUpdateGenreDto

sealed class UpdateGenreState {
    data object Loading:UpdateGenreState()
    data class Success(val genreDto: ResponseUpdateGenreDto):UpdateGenreState()
    data class Error(val message:String):UpdateGenreState()
}