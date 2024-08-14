package com.kseb.smart_car.presentation.main.music

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kseb.smart_car.data.responseDto.ResponseFavoriteMusicDto
import com.kseb.smart_car.data.responseDto.ResponseMusicDto
import com.kseb.smart_car.domain.repository.AuthRepository
import com.kseb.smart_car.extension.AccessState
import com.kseb.smart_car.extension.ChangeFavoriteMusicState
import com.kseb.smart_car.extension.GetFavoriteMusicState
import com.spotify.protocol.types.PlayerState
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
class PlayViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    var nextMusicList: List<ResponseMusicDto.MusicListDto> = mutableListOf()
    private val _favoriteMusicList = MutableLiveData<ResponseFavoriteMusicDto>()
    val favoriteMusicList: MutableLiveData<ResponseFavoriteMusicDto> get() = _favoriteMusicList

    private val _favoriteMusicListState =
        MutableStateFlow<GetFavoriteMusicState>(GetFavoriteMusicState.Loading)
    val favoriteMusicState: StateFlow<GetFavoriteMusicState> get() = _favoriteMusicListState.asStateFlow()

    private val _addFavoriteState =
        MutableStateFlow<ChangeFavoriteMusicState>(ChangeFavoriteMusicState.Loading)
    val addFavoriteState: StateFlow<ChangeFavoriteMusicState> get() = _addFavoriteState.asStateFlow()

    private val _deleteFavoriteState =
        MutableStateFlow<ChangeFavoriteMusicState>(ChangeFavoriteMusicState.Loading)
    val deleteFavoriteState: StateFlow<ChangeFavoriteMusicState> get() = _deleteFavoriteState.asStateFlow()

    private val _isLoginSpotify = MutableLiveData<Boolean>(false)
    val isLoginSpotify: MutableLiveData<Boolean> get() = _isLoginSpotify

    private var accessToken: String? = null

    init {
        setList()
    }

    private fun setList() {
        val list = listOf(
            ResponseMusicDto.MusicListDto("spotify:album:4m2880jivSbbyEGAKfITCa", 0, "s"),
            ResponseMusicDto.MusicListDto("spotify:artist:3WrFJ7ztbogyGnTHbHJFl2", 0, "s"),
            ResponseMusicDto.MusicListDto("spotify:album:4m2880jivSbbyEGAKfITCa", 0, "s"),
            ResponseMusicDto.MusicListDto("spotify:playlist:37i9dQZEVXbMDoHDwVN2tF", 0, "s"),
            ResponseMusicDto.MusicListDto("spotify:show:2tgPYIeGErjk6irHRhk9kj", 0, "s")
        )
        nextMusicList = list
    }

    fun setAccessToken(token:String){
        accessToken=token
    }

    fun loginSpotify() {
        _isLoginSpotify.value = true
    }

    fun getFavoriteMusicList() {
        viewModelScope.launch {
            authRepository.getFavoriteMusic(accessToken!!).onSuccess { response ->
                _favoriteMusicListState.value = GetFavoriteMusicState.Success(response)
                Log.d("playViewmodel", "getfavoritemusic success\n${response.favoritesMusicList}")
            }.onFailure {
                _favoriteMusicListState.value =
                    GetFavoriteMusicState.Error("Error response failure: ${it.message}")
                Log.e("playViewmodel", "Error:${it.message}")
                Log.e("playViewmodel", Log.getStackTraceString(it))
                if (it is HttpException) {
                    try {
                        val errorBody: ResponseBody? = it.response()?.errorBody()
                        val errorBodyString = errorBody?.string() ?: ""

                        // JSONObject를 사용하여 메시지 추출
                        val jsonObject = JSONObject(errorBodyString)
                        val errorMessage = jsonObject.optString("errMsg", "Unknown error")

                        // 추출된 에러 메시지 로깅
                        Log.e("allviewmodel", "Error message: $errorMessage")
                    } catch (e: Exception) {
                        // JSON 파싱 실패 시 로깅
                        Log.e("allviewmodel", "Error parsing error body", e)
                    }
                }
            }
        }
    }

    fun addFavoriteMusicList(playerState: PlayerState) {
        val rawImageUri = playerState.track.imageUri.toString()
        val imageId = rawImageUri
            .replace("ImageId{spotify:image:", "")
            .substringBeforeLast("'")

        viewModelScope.launch {
            authRepository.addFavoriteMusic(accessToken!!, playerState.track.uri, playerState.track.name,
                playerState.track.artist.name, imageId
            ).onSuccess { response->
                _addFavoriteState.value=ChangeFavoriteMusicState.Success(response.response)
                Log.d("playViewModel","add favorite music success")
            }.onFailure {
                _addFavoriteState.value =
                    ChangeFavoriteMusicState.Error("Error response failure: ${it.message}")
                Log.e("playviewmodel", "Error:${it.message}")
                Log.e("playviewmodel", Log.getStackTraceString(it))
                if (it is HttpException) {
                    try {
                        val errorBody: ResponseBody? = it.response()?.errorBody()
                        val errorBodyString = errorBody?.string() ?: ""

                        // JSONObject를 사용하여 메시지 추출
                        val jsonObject = JSONObject(errorBodyString)
                        val errorMessage = jsonObject.optString("errMsg", "Unknown error")

                        // 추출된 에러 메시지 로깅
                        Log.e("allviewmodel", "Error message: $errorMessage")
                    } catch (e: Exception) {
                        // JSON 파싱 실패 시 로깅
                        Log.e("allviewmodel", "Error parsing error body", e)
                    }
                }
            }

        }
    }

    fun deleteFavoriteMusicList(trackId:String) {
        viewModelScope.launch {
            authRepository.deleteFavoriteMusic(accessToken!!,trackId).onSuccess { response->
                _deleteFavoriteState.value=ChangeFavoriteMusicState.Success(response.response)
                Log.d("playViewModel","delete favorite music success")
            }.onFailure {
                _deleteFavoriteState.value =
                    ChangeFavoriteMusicState.Error("Error response failure: ${it.message}")
                Log.e("playviewmodel", "Error:${it.message}")
                Log.e("playviewmodel", Log.getStackTraceString(it))
                if (it is HttpException) {
                    try {
                        val errorBody: ResponseBody? = it.response()?.errorBody()
                        val errorBodyString = errorBody?.string() ?: ""

                        // JSONObject를 사용하여 메시지 추출
                        val jsonObject = JSONObject(errorBodyString)
                        val errorMessage = jsonObject.optString("errMsg", "Unknown error")

                        // 추출된 에러 메시지 로깅
                        Log.e("allviewmodel", "Error message: $errorMessage")
                    } catch (e: Exception) {
                        // JSON 파싱 실패 시 로깅
                        Log.e("allviewmodel", "Error parsing error body", e)
                    }
                }
            }

        }
    }
}