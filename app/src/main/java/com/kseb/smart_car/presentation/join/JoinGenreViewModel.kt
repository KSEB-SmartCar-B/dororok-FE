package com.kseb.smart_car.presentation.join

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kseb.smart_car.R
import com.kseb.smart_car.domain.repository.AuthRepository
import com.kseb.smart_car.extension.AllGenreState
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
class JoinGenreViewModel @Inject constructor(
    val authRepository: AuthRepository
): ViewModel() {
    private val _genreListState = MutableStateFlow<AllGenreState>(AllGenreState.Loading)
    val genreListState:StateFlow<AllGenreState> = _genreListState.asStateFlow()

    fun getGenreList(){
        viewModelScope.launch {
            authRepository.getGenreList().onSuccess {
                _genreListState.value = AllGenreState.Success(it)
            }.onFailure {
                _genreListState.value = AllGenreState.Error("Error response failure: ${it.message}")

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

  /*  fun makeList(): List<Genre> {
        return listOf(
            Genre("댄스", R.drawable.genre_dance),
            Genre("발라드", R.drawable.genre_balad),
            Genre("인디", R.drawable.genre_indi),
            Genre("트로트", R.drawable.genre_trot),
            Genre("OST", R.drawable.genre_ost),
            Genre("POP", R.drawable.genre_pop),
            Genre("J-POP", R.drawable.genre_jpop),
            Genre("재즈", R.drawable.genre_jazz),
            Genre("클래식", R.drawable.genre_classic),
            Genre("뉴에이지", R.drawable.genre_newage),
            Genre("일렉트로니카", R.drawable.genre_electronic),
            Genre("국내 밴드", R.drawable.genre_kband),
            Genre("해외 밴드", R.drawable.genre_band),
            Genre("국내 랩/힙합", R.drawable.genre_khiphop),
            Genre("해외 랩/힙합", R.drawable.genre_hiphop),
            Genre("국내 록/메탈", R.drawable.genre_krock),
            Genre("해외 록/메탈", R.drawable.genre_rock),
            Genre("국내 R&B/Soul", R.drawable.genre_krnb),
            Genre("해외 R&B/Soul", R.drawable.genre_rnb),
            Genre("국내 포크/블루스", R.drawable.genre_kfolk),
            Genre("해외 포크/블루스/컨트리", R.drawable.genre_folk),
        )
    }*/
}