package com.kseb.smart_car.extension

sealed class DeleteMusicListState {
    data object Loading:DeleteMusicListState()
    data class Success(val response :String):DeleteMusicListState()
    data class Error(val message:String):DeleteMusicListState()
}