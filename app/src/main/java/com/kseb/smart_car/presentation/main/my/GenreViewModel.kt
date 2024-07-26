package com.kseb.smart_car.presentation.main.my

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kseb.smart_car.domain.repository.AuthRepository
import com.kseb.smart_car.extension.GenreState
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

    private val genre = MutableLiveData<List<String>>()

    //통신 성공했는지 보는 거
    private val _genreState = MutableStateFlow<GenreState>(GenreState.Loading)
    val genreState: StateFlow<GenreState> = _genreState.asStateFlow()

    fun getMyGenre(token:String) {
        viewModelScope.launch {
            authRepository.getMyGenre(token).onSuccess { response ->
                _genreState.value=GenreState.Success(response)
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
        val currentList = genre.value ?: emptyList()
        val newList = currentList.toMutableList()
        if (text in newList) {
            newList.remove(text)
        } else newList.add(text)
        genre.value = newList

        //잘 되는지 확인용
        Log.d("genreViewModel", "Genre: ${this.genre.value}")
    }

//    fun updateGenre(token: String) {
//        viewModelScope.launch {
//            authRepository.updateGenre(token, )
//        }
//    }
}