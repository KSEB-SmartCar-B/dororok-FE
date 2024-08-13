package com.kseb.smart_car.presentation.main.my

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kseb.smart_car.data.requestDto.RequestUpdateGenreDto
import com.kseb.smart_car.data.requestDto.RequestUpdateInfoDto
import com.kseb.smart_car.data.responseDto.ResponseMyGenreDto
import com.kseb.smart_car.data.responseDto.ResponseUpdateGenreDto
import com.kseb.smart_car.domain.repository.AuthRepository
import com.kseb.smart_car.extension.GenreState
import com.kseb.smart_car.extension.UpdateGenreState
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
class GenreViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val genre = MutableLiveData<RequestUpdateGenreDto>()

    //통신 성공했는지 보는 거
    private val _genreState = MutableStateFlow<GenreState>(GenreState.Loading)
    val genreState: StateFlow<GenreState> = _genreState.asStateFlow()

    private val _updateGenreState= MutableStateFlow<UpdateGenreState>(UpdateGenreState.Loading)
    val updateGenreState:StateFlow<UpdateGenreState> = _updateGenreState.asStateFlow()

    fun getMyGenre(token:String) {
        viewModelScope.launch {
            authRepository.getMyGenre(token).onSuccess { response ->
                _genreState.value=GenreState.Success(response)
                genre.value=RequestUpdateGenreDto(response.favoriteGenres)
            }.onFailure{
                _genreState.value=GenreState.Error("Error response failure: ${it.message}")

                Log.e("genreViewModel", "Error:${it.message}")
                Log.e("genreViewModel", Log.getStackTraceString(it))
                if (it is HttpException) {
                    try {
                        val errorBody: ResponseBody? = it.response()?.errorBody()
                        val errorBodyString = errorBody?.string() ?: ""

                        // JSONObject를 사용하여 메시지 추출
                        val jsonObject = JSONObject(errorBodyString)
                        val errorMessage = jsonObject.optString("errMsg", "Unknown error")

                        // 추출된 에러 메시지 로깅
                        Log.e("genreViewModel", "Error message: $errorMessage")
                    } catch (e: Exception) {
                        // JSON 파싱 실패 시 로깅
                        Log.e("genreViewModel", "Error parsing error body", e)
                    }
                }
            }
        }
    }

    fun getGenre(text: String) {
        // 현재 장르 리스트 가져오기
        val currentList = genre.value?.favoriteGenres?.toMutableList() ?: mutableListOf()
        Log.d("genreViewModel", "Current list: $currentList")

        // 리스트 수정
        val genreName = ResponseMyGenreDto.Name(text)
        if (currentList.contains(genreName)) {
            currentList.remove(genreName)
        } else {
            currentList.add(genreName)
        }

        // 업데이트된 리스트로 RequestUpdateGenreDto 객체 갱신
        genre.value = RequestUpdateGenreDto(favoriteGenres = currentList)

        // 로그 출력
        Log.d("genreViewModel", "Updated Genre List: ${this.genre.value?.favoriteGenres}")
    }


    fun setGenre(token:String){
        viewModelScope.launch {
            authRepository.updateGenre(token, genre.value!!).onSuccess {response->
                _updateGenreState.value=UpdateGenreState.Success(response)
                Log.d("genreViewModel","set genre success")
            }.onFailure {
                _updateGenreState.value=UpdateGenreState.Error("Error response failure: ${it.message}")

                Log.e("genreViewModel", "Error:${it.message}")
                Log.e("genreViewModel", Log.getStackTraceString(it))
                if (it is HttpException) {
                    try {
                        val errorBody: ResponseBody? = it.response()?.errorBody()
                        val errorBodyString = errorBody?.string() ?: ""

                        // JSONObject를 사용하여 메시지 추출
                        val jsonObject = JSONObject(errorBodyString)
                        val errorMessage = jsonObject.optString("errMsg", "Unknown error")

                        // 추출된 에러 메시지 로깅
                        Log.e("genreViewModel", "Error message: $errorMessage")
                    } catch (e: Exception) {
                        // JSON 파싱 실패 시 로깅
                        Log.e("genreViewModel", "Error parsing error body", e)
                    }
                }
            }
        }
    }
}