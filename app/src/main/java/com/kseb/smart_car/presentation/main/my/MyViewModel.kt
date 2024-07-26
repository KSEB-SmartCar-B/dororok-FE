package com.kseb.smart_car.presentation.main.my

import android.icu.text.IDNA.Info
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kseb.smart_car.data.responseDto.ResponseMyInfoDto
import com.kseb.smart_car.domain.repository.AuthRepository
import com.kseb.smart_car.extension.GenreState
import com.kseb.smart_car.extension.InfoState
import com.kseb.smart_car.presentation.join.Genre
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(
    private val authRepository: AuthRepository
): ViewModel() {
    private val _infoState = MutableStateFlow<InfoState>(InfoState.Loading)
    val infoState:StateFlow<InfoState> = _infoState.asStateFlow()

    private var info: ResponseMyInfoDto? = null

    private val _selectedGenres = MutableStateFlow<Set<String>>(emptySet())
    val selectedGenres: StateFlow<Set<String>> = _selectedGenres.asStateFlow()

    private val _buttonText = MutableLiveData<String>()
    val buttonText: LiveData<String> = _buttonText

    var token: String? = null

    fun getInfo(token:String){
        Log.d("myviewmodel","getinfo start\ntoken:${token}")
        viewModelScope.launch {
            authRepository.getMyInfo(token).onSuccess { response ->
                Log.d("myviewmodel","response->${response}")
                _infoState.value=InfoState.Success(response)
                info=response
            }.onFailure{
                _infoState.value=InfoState.Error("Error response failure: ${it.message}")

                Log.e("myViewModel", "Error:${it.message}")
                Log.e("myViewModel", Log.getStackTraceString(it))
                if (it is HttpException) {
                    try {
                        val errorBody: ResponseBody? = it.response()?.errorBody()
                        val errorBodyString = errorBody?.string() ?: ""

                        // JSONObject를 사용하여 메시지 추출
                        val jsonObject = JSONObject(errorBodyString)
                        val errorMessage = jsonObject.optString("errMsg", "Unknown error")

                        // 추출된 에러 메시지 로깅
                        Log.e("myViewModel", "Error message: $errorMessage")
                    } catch (e: Exception) {
                        // JSON 파싱 실패 시 로깅
                        Log.e("myViewModel", "Error parsing error body", e)
                    }
                }
            }
        }
    }

    fun getMyInfo():String? = info?.let {
        Json.encodeToString(it)
    }

    fun updateGenre(newGenre: String) {
        val updatedGenres = _selectedGenres.value.toMutableSet()
        if (updatedGenres.contains(newGenre)) {
            updatedGenres.remove(newGenre)
        } else {
            updatedGenres.add(newGenre)
        }
        _selectedGenres.value = updatedGenres
    }

    fun updateInfo(){

    }
    //var genre = mutableListOf("댄스", "POP")
    /*var gender = "여자"
    var nickname = "이지원"
    var birthYear = "1999"
    var birthMonth = "2"
    var birthDay = "14"
<<<<<<< HEAD
    var birth: LocalDateTime? = null*/


   /* fun getInfo(
        gender: String,
        nickname: String,
        birthYear: String,
        birthMonth: String,
        birthDay: String
    ) {
        this.gender = gender
        this.nickname = nickname
        birth = createLocalDate(birthYear, birthMonth, birthDay)


        Log.d(
            "MyViewModel",
            "Gender: ${this.gender}, Nickname: ${this.nickname}, Birth: ${this.birth}}"
        )
    }*/

    /*fun getGenre(text: String) {
        val currentList = genre ?: emptyList()
        val newList = currentList.toMutableList()
        if (text in newList) {
            newList.remove(text)
        } else newList.add(text)
        genre = newList

        //잘 되는지 확인용
        Log.d("MyViewModel", "Genre: ${this.genre}")
    }*/
}