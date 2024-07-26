package com.kseb.smart_car.presentation.join

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kakao.sdk.user.model.Gender
import com.kseb.smart_car.data.requestDto.RequestSignUpDto
import com.kseb.smart_car.domain.repository.AuthRepository
import com.kseb.smart_car.extension.SignInState
import com.kseb.smart_car.extension.SignUpState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class JoinViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    //회원가입상태
    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Loading)
    val signUpState:StateFlow<SignUpState> = _signUpState.asStateFlow()
    private var kakaoToken:String = null.toString()

    private val _buttonText = MutableLiveData<String>()
    val buttonText: LiveData<String> = _buttonText

    private val gender = MutableLiveData<String>()
    private val nickname = MutableLiveData<String>()
//    private val birthYear = MutableLiveData<String>()
//    private val birthMonth = MutableLiveData<String>()
//    private val birthDay = MutableLiveData<String>()
    private val birth = MutableLiveData<LocalDate?>()
    private val genre = MutableLiveData<List<String>>()

    fun setKakaoToken(token:String){
        kakaoToken=token
    }

    fun getInfo(
        gender: String,
        nickname: String,
        birthYear: String,
        birthMonth: String,
        birthDay: String
    ) {
        this.gender.value = gender
        this.nickname.value = nickname
        birth.value = createLocalDate(birthYear, birthMonth, birthDay)

        //잘 되는지 확인용
        Log.d(
            "JoinViewModel",
            "Gender: ${this.gender.value}, Nickname: ${this.nickname.value}, Birth: ${this.birth.value}}"
        )
    }

    fun getGenre(text: String) {
        val currentList = genre.value ?: emptyList()
        val newList = currentList.toMutableList()
        if (text in newList) {
            newList.remove(text)
        } else newList.add(text)
        genre.value = newList

        //잘 되는지 확인용
        Log.d("JoinViewModel", "Genre: ${this.genre.value}")
    }

    private fun createLocalDate(birthYear: String, birthMonth: String, birthDay: String): LocalDate? {
        val year = birthYear.toInt()
        val month = birthMonth.toInt()+1
        val day = birthDay.toInt()

        return LocalDate.of(year, month, day) // 시간을 00:00으로 설정
    }

    fun makeSignUp(){
        viewModelScope.launch {
            Log.d("joinViewModel","kakaoToken:${kakaoToken}")
            authRepository.getSignUp(RequestSignUpDto(kakaoToken, nickname.value!!,
                "FEMALE", birth.value!!, true, true, genre.value!!)).onSuccess { response ->
                _signUpState.value=SignUpState.Success(response.accessToken)
            }.onFailure {
                _signUpState.value=SignUpState.Error("Error response failure: ${it.message}")

                if (it is HttpException) {
                    try {
                        val errorBody: ResponseBody? = it.response()?.errorBody()
                        val errorBodyString = errorBody?.string() ?: ""

                        // JSONObject를 사용하여 메시지 추출
                        val jsonObject = JSONObject(errorBodyString)
                        val errorMessage = jsonObject.optString("errMsg", "Unknown error")

                        // 추출된 에러 메시지 로깅
                        Log.e("joinViewModel", "Error message: $errorMessage")
                    } catch (e: Exception) {
                        // JSON 파싱 실패 시 로깅
                        Log.e("joinViewModel", "Error parsing error body", e)
                    }
                }
            }
        }
    }
}