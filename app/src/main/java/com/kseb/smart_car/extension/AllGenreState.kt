package com.kseb.smart_car.extension

import com.kseb.smart_car.data.responseDto.ResponseAllGenreDto
import com.kseb.smart_car.data.responseDto.ResponseUpdateGenreDto

sealed class AllGenreState {
    data object Loading:AllGenreState()
    data class Success(val genreDto: ResponseAllGenreDto):AllGenreState()
    data class Error(val message:String):AllGenreState()

}