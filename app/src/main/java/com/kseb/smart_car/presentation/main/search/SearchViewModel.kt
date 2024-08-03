package com.kseb.smart_car.presentation.main.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kseb.smart_car.BuildConfig
import com.kseb.smart_car.data.requestDto.RequestAddSearchDto
import com.kseb.smart_car.data.requestDto.RequestDeleteSearchDto
import com.kseb.smart_car.domain.repository.AuthRepository
import com.kseb.smart_car.domain.repository.KakaoRepository
import com.kseb.smart_car.extension.AddSearchState
import com.kseb.smart_car.extension.AddressState
import com.kseb.smart_car.extension.DeleteSearchState
import com.kseb.smart_car.extension.GetSearchState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import java.lang.Thread.State
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val authRepository: AuthRepository, private val kakaoRepository: KakaoRepository
) : ViewModel() {
    private val _accessToken = MutableLiveData<String>()
    private val _goalState = MutableLiveData<Goal>()
    val accessToken: MutableLiveData<String> get() = _accessToken
    val goalState:MutableLiveData<Goal> get()=_goalState

    private val _searchState = MutableStateFlow<GetSearchState>(GetSearchState.Loading)
    private val _addSearchState = MutableStateFlow<AddSearchState>(AddSearchState.Loading)
    private val _deleteSearchState = MutableStateFlow<DeleteSearchState>(DeleteSearchState.Loading)
    private val _addressState = MutableStateFlow<AddressState>(AddressState.Loading)
    val searchState: StateFlow<GetSearchState> = _searchState.asStateFlow()
    val addSearchState: StateFlow<AddSearchState> = _addSearchState.asStateFlow()
    val deleteSearchState: StateFlow<DeleteSearchState> = _deleteSearchState.asStateFlow()
    val addressState: StateFlow<AddressState> = _addressState.asStateFlow()

    data class Goal(
        val x:Double,
        val y:Double
    )

    fun setAccessToken(token: String) {
        _accessToken.value = token
    }

    fun setGoalCoordinate(x:Double,y:Double){
        _goalState.value= Goal(x,y)
        Log.d("searchViewModel","x:$x, y:$y")
    }

    fun getSearch() {
        viewModelScope.launch {
            authRepository.getSearch(_accessToken.value!!).onSuccess { response ->
                _searchState.value = GetSearchState.Success(response)
                Log.d("searchViewModel", "searchState 성공!")
            }.onFailure {
                _searchState.value = GetSearchState.Error("Error response failure: ${it.message}")
                if (it is HttpException) {
                    try {
                        val errorBody: ResponseBody? = it.response()?.errorBody()
                        val errorBodyString = errorBody?.string() ?: ""
                        httpError(errorBodyString)
                    } catch (e: Exception) {
                        // JSON 파싱 실패 시 로깅
                        Log.e("searchViewModel", "Error parsing error body", e)
                    }
                }
            }
        }
    }

    fun addSearch(searchLog: String) {
        viewModelScope.launch {
            authRepository.addSearch(_accessToken.value!!, RequestAddSearchDto(searchLog))
                .onSuccess { response ->
                    _addSearchState.value = AddSearchState.Success(response)
                    Log.d("searchViewModel", "addSearch 성공!")
                }.onFailure {
                _addSearchState.value =
                    AddSearchState.Error("Error response failure: ${it.message}")
                if (it is HttpException) {
                    try {
                        val errorBody: ResponseBody? = it.response()?.errorBody()
                        val errorBodyString = errorBody?.string() ?: ""
                        httpError(errorBodyString)
                    } catch (e: Exception) {
                        // JSON 파싱 실패 시 로깅
                        Log.e("searchViewModel", "Error parsing error body", e)
                    }
                } else {
                    Log.e("searchViewModel", "Unknown error: ${it.message}", it)
                }
            }
        }
    }

    fun deleteSearch(searchLog: String) {
        Log.d("searchViewModel", "deleteSearch")
        viewModelScope.launch {
            authRepository.deleteSearch(_accessToken.value!!, RequestDeleteSearchDto(searchLog))
                .onSuccess { response ->
                    _deleteSearchState.value = DeleteSearchState.Success(response)
                    Log.d("searchViewModel", "deleteSearch 성공!")
                }.onFailure {
                _deleteSearchState.value =
                    DeleteSearchState.Error("Error response failure: ${it.message}")
                if (it is HttpException) {
                    try {
                        val errorBody: ResponseBody? = it.response()?.errorBody()
                        val errorBodyString = errorBody?.string() ?: ""
                        httpError(errorBodyString)
                    } catch (e: Exception) {
                        // JSON 파싱 실패 시 로깅
                        Log.e("searchViewModel", "Error parsing error body", e)
                    }
                }
            }
        }
    }

    fun setSearchLoading(state: String) {
        when (state) {
            "get" -> {
                _searchState.value = GetSearchState.Loading
            }

            "add" -> {
                _addSearchState.value = AddSearchState.Loading
            }

            "delete" -> {
                _deleteSearchState.value = DeleteSearchState.Loading
            }

            "address" -> {
                _addressState.value = AddressState.Loading
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
                Log.d("searchViewModel", "addressState 성공!")
            }.onFailure {
                _addressState.value = AddressState.Error("Error response failure: ${it.message}")
                if (it is HttpException) {
                    try {
                        val errorBody: ResponseBody? = it.response()?.errorBody()
                        val errorBodyString = errorBody?.string() ?: ""
                        httpError(errorBodyString)
                    } catch (e: Exception) {
                        // JSON 파싱 실패 시 로깅
                        Log.e("searchViewModel", "Error parsing error body", e)
                    }
                }
            }
        }
    }

    private fun httpError(errorBody: String) {
        // 전체 에러 바디를 로깅하여 디버깅
        Log.e("searchViewModel", "Full error body: $errorBody")

        // JSONObject를 사용하여 메시지 추출
        val jsonObject = JSONObject(errorBody)
        val errorMessage = jsonObject.optString("message", "Unknown error")

        // 추출된 에러 메시지 로깅
        Log.e("searchViewModel", "Error message: $errorMessage")
    }

}