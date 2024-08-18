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
import com.kseb.smart_car.extension.RecommendPlaceState
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
    private val _accessToken = MutableLiveData<String>()
    val accessToken: MutableLiveData<String> get() = _accessToken

    private val _placeNearbyState =
        MutableStateFlow<RecommendPlaceNearbyState>(RecommendPlaceNearbyState.Loading)
    val placeNearbyState: StateFlow<RecommendPlaceNearbyState> = _placeNearbyState.asStateFlow()
    private val _placeState =
        MutableStateFlow<RecommendPlaceState>(RecommendPlaceState.Loading)
    val placeState: StateFlow<RecommendPlaceState> = _placeState.asStateFlow()

    private val _savedPlaceList =
        MutableLiveData<List<ResponseFavoritePlaceDto.FavoritesPlaceListDto>>()
    val savedPlaceList: MutableLiveData<List<ResponseFavoritePlaceDto.FavoritesPlaceListDto>> get() = _savedPlaceList

    private val _addNearbyState =
        MutableStateFlow<AddFavoritePlaceState>(AddFavoritePlaceState.Loading)
    val addNearbyState: StateFlow<AddFavoritePlaceState> = _addNearbyState.asStateFlow()
    private val _deleteNearbyState =
        MutableStateFlow<DeleteFavoritePlaceState>(DeleteFavoritePlaceState.Loading)
    val deleteNearbyState: StateFlow<DeleteFavoritePlaceState> = _deleteNearbyState.asStateFlow()

    var isLoadingNear = false
    var isLastPageNear = false
    var pageNoNear = 1
    var pageSizeNear: Int? = null  // 한 페이지당 아이템 개수
    var totalPageNoNear: Int? = null

    var isLoadingPlace = false
    var isLastPagePlace = false
    var pageNoPlace = 1
    var pageSizePlace: Int? = null  // 한 페이지당 아이템 개수
    var totalPageNoPlace: Int? = null

    fun setAccessToken(token: String) {
        accessToken.value = token
    }

    fun getSavedPlaceList() {
        viewModelScope.launch {
            authRepository.getFavoritePlace(_accessToken.value!!).onSuccess { response ->
                _savedPlaceList.value = response.favoritesPlaceList
                Log.d("placeViewModel", "favoriteplacelist get success")
            }.onFailure {
                Log.e("placeViewModel", "getting favorite place list fail!")
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

    fun addFavoritePlace(title: String, address: String, imageUri: String, contentId: String) {
        viewModelScope.launch {
            authRepository.saveFavoritePlace(
                _accessToken.value!!,
                title,
                address,
                imageUri,
                contentId
            ).onSuccess { response ->
                _addNearbyState.value = AddFavoritePlaceState.Success(response.response)
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

    fun deleteFavoritePlace(contentId: String) {
        viewModelScope.launch {
            authRepository.deleteFavoritePlace(_accessToken.value!!, contentId)
                .onSuccess { response ->
                    _deleteNearbyState.value = DeleteFavoritePlaceState.Success(response.response)
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

    fun getPlaceNearby(lat: Double, lng: Double,page: Int = 1) {
        viewModelScope.launch {
            authRepository.getRecommendPlaceNearby(
                _accessToken.value!!,
                lat.toString(),
                lng.toString(),
                page
            )
                .onSuccess { response ->
                    _placeNearbyState.value = RecommendPlaceNearbyState.Success(response)
                    totalPageNoNear = response.pageNumbers
                    pageSizeNear = response.places.size

                    // 마지막 페이지 체크
                    if (pageNoNear >= totalPageNoNear!!) {
                        isLastPageNear = true
                    }
                    Log.d ("placeviewmodel", "recommend place nearby 성공!:${totalPageNoNear}, ${pageNoNear} = $page ${response.places}")
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

    fun getPlace() {
        viewModelScope.launch {
            authRepository.getRecommendPlace(_accessToken.value!!,).onSuccess { response ->
                    _placeState.value = RecommendPlaceState.Success(response)
                    totalPageNoPlace = response.pageNumbers
                    pageSizePlace = response.places.size

                    // 마지막 페이지 체크
                    if (pageNoPlace >= totalPageNoPlace!!) {
                        isLastPagePlace = true
                    }
                    Log.d("placeviewmodel", "recommend place 성공!${response.places}")
                }.onFailure {
                _placeState.value =
                        RecommendPlaceState.Error("Error response failure: ${it.message}")
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

    fun setSaveLoading() {
        _addNearbyState.value = AddFavoritePlaceState.Loading
    }

    fun setDeleteLoading() {
        _deleteNearbyState.value = DeleteFavoritePlaceState.Loading
    }
}