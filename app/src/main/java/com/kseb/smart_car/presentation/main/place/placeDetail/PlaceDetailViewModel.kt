package com.kseb.smart_car.presentation.main.place.placeDetail

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kseb.smart_car.BuildConfig
import com.kseb.smart_car.domain.repository.AuthRepository
import com.kseb.smart_car.domain.repository.KakaoRepository
import com.kseb.smart_car.extension.AddSearchState
import com.kseb.smart_car.extension.AddressState
import com.kseb.smart_car.extension.DeleteSearchState
import com.kseb.smart_car.extension.ExistFavoritePlaceState
import com.kseb.smart_car.extension.GetRecommendPlaceDetailState
import com.kseb.smart_car.extension.GetSearchState
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
class PlaceDetailViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val kakaoRepository: KakaoRepository
):ViewModel() {
    private val _accessToken=MutableLiveData<String>()
    val accessToken:MutableLiveData<String> get() = _accessToken

    private val _existState = MutableStateFlow<ExistFavoritePlaceState>(ExistFavoritePlaceState.Loading)
    val existState:MutableStateFlow<ExistFavoritePlaceState> = _existState

    private val _addressState = MutableStateFlow<AddressState>(AddressState.Loading)
    val addressState: StateFlow<AddressState> = _addressState.asStateFlow()

    private val _detailState=MutableStateFlow<GetRecommendPlaceDetailState>(GetRecommendPlaceDetailState.Loading)
    val detailState:StateFlow<GetRecommendPlaceDetailState> = _detailState.asStateFlow()

    fun setAccessToken(token:String){
        _accessToken.value=token
    }

    fun existFavoritePlace(token:String, contentId:String){
        viewModelScope.launch {
            authRepository.existFavoritePlace(token, contentId).onSuccess { response->
                _existState.value=ExistFavoritePlaceState.Success(response.isExisted)
                Log.d("placeDetailViewModel","exist state success")
            }.onFailure {
                _existState.value=ExistFavoritePlaceState.Error("Error response failure: ${it.message}")

                Log.e("placeDetailViewModel", "Error:${it.message}")
                Log.e("placeDetailViewModel", Log.getStackTraceString(it))
                if (it is HttpException) {
                    try {
                        val errorBody: ResponseBody? = it.response()?.errorBody()
                        val errorBodyString = errorBody?.string() ?: ""

                        // JSONObject를 사용하여 메시지 추출
                        val jsonObject = JSONObject(errorBodyString)
                        val errorMessage = jsonObject.optString("errMsg", "Unknown error")

                        // 추출된 에러 메시지 로깅
                        Log.e("placeDetailViewModel", "Error message: $errorMessage")
                    } catch (e: Exception) {
                        // JSON 파싱 실패 시 로깅
                        Log.e("placeDetailViewModel", "Error parsing error body", e)
                    }
                }
            }
        }
    }

    //장소 찾기
    fun getAddress(address: String) {
        val api = "KakaoAK ${BuildConfig.KAKAO_REST_API}"
        Log.d("searchViewModel", "api key: ${api}")

        viewModelScope.launch {
            kakaoRepository.getAddress(api, address).onSuccess { response ->
                _addressState.value = AddressState.Success(response)
                Log.d("placeDetailViewModel", "addressState 성공!")
            }.onFailure {
                _addressState.value = AddressState.Error("Error response failure: ${it.message}")
                Log.e("placeDetailViewModel", "Error:${it.message}")
                Log.e("placeDetailViewModel", Log.getStackTraceString(it))
                if (it is HttpException) {
                    try {
                        val errorBody: ResponseBody? = it.response()?.errorBody()
                        val errorBodyString = errorBody?.string() ?: ""

                        // JSONObject를 사용하여 메시지 추출
                        val jsonObject = JSONObject(errorBodyString)
                        val errorMessage = jsonObject.optString("errMsg", "Unknown error")

                        // 추출된 에러 메시지 로깅
                        Log.e("placeDetailViewModel", "Error message: $errorMessage")
                    } catch (e: Exception) {
                        // JSON 파싱 실패 시 로깅
                        Log.e("placeDetailViewModel", "Error parsing error body", e)
                    }
                }
            }
        }
    }

    fun getDetail(token:String, contentId:String){
        Log.d("placedetailviewmodel","getdetail start:${token}, ${contentId}")

        viewModelScope.launch {
            authRepository.getRecommendPlaceNearbyDetail(token,contentId).onSuccess { response->
                _detailState.value=GetRecommendPlaceDetailState.Success(response)
                Log.d("placeDetailViewModel", "GetRecommendPlaceDetailState 성공!")
            }.onFailure {
                _detailState.value = GetRecommendPlaceDetailState.Error("Error response failure: ${it.message}")
                Log.e("placeDetailViewModel", "Error:${it.message}")
                Log.e("placeDetailViewModel", Log.getStackTraceString(it))
                if (it is HttpException) {
                    try {
                        val errorBody: ResponseBody? = it.response()?.errorBody()
                        val errorBodyString = errorBody?.string() ?: ""

                        // JSONObject를 사용하여 메시지 추출
                        val jsonObject = JSONObject(errorBodyString)
                        val errorMessage = jsonObject.optString("errMsg", "Unknown error")

                        // 추출된 에러 메시지 로깅
                        Log.e("placeDetailViewModel", "Error message: $errorMessage")
                    } catch (e: Exception) {
                        // JSON 파싱 실패 시 로깅
                        Log.e("placeDetailViewModel", "Error parsing error body", e)
                    }
                }
            }
        }
    }
}