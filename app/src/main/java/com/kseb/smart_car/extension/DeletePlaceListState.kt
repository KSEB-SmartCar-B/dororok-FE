package com.kseb.smart_car.extension

sealed class DeletePlaceListState {
    data object Loading:DeletePlaceListState()
    data class Success(val response :String):DeletePlaceListState()
    data class Error(val message:String):DeletePlaceListState()
}