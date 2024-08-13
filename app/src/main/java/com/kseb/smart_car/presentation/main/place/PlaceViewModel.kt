package com.kseb.smart_car.presentation.main.place

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kseb.smart_car.data.responseDto.ResponseFavoritePlaceDto
import com.kseb.smart_car.domain.repository.AuthRepository
import com.kseb.smart_car.extension.DeleteFavoritePlaceState
import com.kseb.smart_car.extension.RecommendPlaceNearbyState
import com.kseb.smart_car.extension.AddFavoritePlaceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class PlaceViewModel @Inject constructor(
    val authRepository: AuthRepository
) : ViewModel() {
    private val _placeNearbyState =
        MutableStateFlow<RecommendPlaceNearbyState>(RecommendPlaceNearbyState.Loading)
    val placeNearbyState: StateFlow<RecommendPlaceNearbyState> = _placeNearbyState.asStateFlow()

    private val _savedPlaceList = MutableLiveData<List<ResponseFavoritePlaceDto.FavoritesPlaceListDto>>()
    val savedPlaceList:MutableLiveData<List<ResponseFavoritePlaceDto.FavoritesPlaceListDto>> get() = _savedPlaceList

    private val _addNearbyState = MutableStateFlow<AddFavoritePlaceState>(AddFavoritePlaceState.Loading)
    val addNearbyState:StateFlow<AddFavoritePlaceState> = _addNearbyState.asStateFlow()
    private val _deleteNearbyState = MutableStateFlow<DeleteFavoritePlaceState>(DeleteFavoritePlaceState.Loading)
    val deleteNearbyState:StateFlow<DeleteFavoritePlaceState> = _deleteNearbyState.asStateFlow()

    private var pageNo=1
    private var totalPageNo=1

    fun getSavedPlaceList(accessToken: String){
        viewModelScope.launch {
            authRepository.getFavoritePlace(accessToken).onSuccess {response ->
                _savedPlaceList.value=response.favoritesPlaceList
                Log.d("placeViewModel","favoriteplacelist get success")
            }.onFailure {
                Log.e("placeViewModel","getting favorite place list fail!")
                if (it is HttpException) {
                    try {
                        val errorBody: ResponseBody? = it.response()?.errorBody()
                        val errorBodyString = errorBody?.string() ?: ""

                        Log.e("placeviewmodel", "Full error body: $errorBodyString")

                        val jsonObject = JSONObject(errorBodyString)
                        val errorMessage = jsonObject.optString("message", "Unknown error")

                        Log.e("placeviewmodel", "Error message: $errorMessage")
                    } catch (e: Exception) {
                        Log.e("placeviewmodel", "Error parsing error body", e)
                    }
                } else {
                    Log.e("placeviewmodel", "Non-HTTP exception: ${it.message}")
                }
            }
        }
    }

    fun addFavoritePlace(accessToken: String,title:String, address:String, imageUri:String, contentId:String){
        viewModelScope.launch {
            authRepository.saveFavoritePlace(accessToken, title, address, imageUri, contentId).onSuccess { response ->
                _addNearbyState.value=AddFavoritePlaceState.Success(response.response)
                Log.d("placeviewmodel", "add place nearby 성공!")
            }.onFailure {
                _addNearbyState.value =
                        AddFavoritePlaceState.Error("Error response failure: ${it.message}")
                if (it is HttpException) {
                    try {
                        val errorBody: ResponseBody? = it.response()?.errorBody()
                        val errorBodyString = errorBody?.string() ?: ""

                        Log.e("placeviewmodel", "Full error body: $errorBodyString")

                        val jsonObject = JSONObject(errorBodyString)
                        val errorMessage = jsonObject.optString("message", "Unknown error")

                        Log.e("placeviewmodel", "Error message: $errorMessage")
                    } catch (e: Exception) {
                        Log.e("placeviewmodel", "Error parsing error body", e)
                    }
                } else {
                    Log.e("placeviewmodel", "Non-HTTP exception: ${it.message}")
                }
            }
        }
    }

    fun deleteFavoritePlace(accessToken: String, contentId:String){
        viewModelScope.launch {
            authRepository.deleteFavoritePlace(accessToken, contentId).onSuccess { response->
                _deleteNearbyState.value=DeleteFavoritePlaceState.Success(response.response)
                Log.d("placeviewmodel", "delete place nearby 성공!")
            }.onFailure {
                _deleteNearbyState.value =
                    DeleteFavoritePlaceState.Error("Error response failure: ${it.message}")
                if (it is HttpException) {
                    try {
                        val errorBody: ResponseBody? = it.response()?.errorBody()
                        val errorBodyString = errorBody?.string() ?: ""

                        Log.e("placeviewmodel", "Full error body: $errorBodyString")

                        val jsonObject = JSONObject(errorBodyString)
                        val errorMessage = jsonObject.optString("message", "Unknown error")

                        Log.e("placeviewmodel", "Error message: $errorMessage")
                    } catch (e: Exception) {
                        Log.e("placeviewmodel", "Error parsing error body", e)
                    }
                } else {
                    Log.e("placeviewmodel", "Non-HTTP exception: ${it.message}")
                }
            }
        }
    }

    fun getPlaceNearby(accessToken: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            authRepository.getRecommendPlaceNearby(accessToken, lat.toString(), lng.toString(), pageNo++)
                .onSuccess { response ->
                    _placeNearbyState.value = RecommendPlaceNearbyState.Success(response)
                    totalPageNo=response.pageNumbers
                    Log.d("placeviewmodel", "recommend place nearby 성공!")
                }.onFailure {
                    _placeNearbyState.value =
                        RecommendPlaceNearbyState.Error("Error response failure: ${it.message}")
                    if (it is HttpException) {
                        try {
                            val errorBody: ResponseBody? = it.response()?.errorBody()
                            val errorBodyString = errorBody?.string() ?: ""

                            Log.e("placeviewmodel", "Full error body: $errorBodyString")

                            val jsonObject = JSONObject(errorBodyString)
                            val errorMessage = jsonObject.optString("message", "Unknown error")

                            Log.e("placeviewmodel", "Error message: $errorMessage")
                        } catch (e: Exception) {
                            Log.e("placeviewmodel", "Error parsing error body", e)
                        }
                    } else {
                        Log.e("placeviewmodel", "Non-HTTP exception: ${it.message}")
                    }
                }
        }
    }

    fun setSaveLoading(){
        _addNearbyState.value=AddFavoritePlaceState.Loading
    }
    fun setDeleteLoading(){
        _deleteNearbyState.value=DeleteFavoritePlaceState.Loading
    }
}