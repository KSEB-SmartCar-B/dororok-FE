package com.kseb.smart_car.presentation.main.music

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kseb.smart_car.data.responseDto.ResponseFavoriteMusicDto
import com.kseb.smart_car.data.responseDto.ResponseMusicDto
import com.kseb.smart_car.data.responseDto.ResponseRecommendMusicDto
import com.kseb.smart_car.domain.repository.AuthRepository
import com.kseb.smart_car.extension.AccessState
import com.kseb.smart_car.extension.ChangeFavoriteMusicState
import com.kseb.smart_car.extension.GetFavoriteMusicState
import com.kseb.smart_car.extension.GetRecommendMusicState
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class PlayViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _accessToken = MutableLiveData<String>()
    val accessToken:MutableLiveData<String> get() = _accessToken
    private var isFirst:Int=1

    private val _spotifyAppRemote = MutableLiveData<SpotifyAppRemote>()
    val spotifyAppRemote:MutableLiveData<SpotifyAppRemote> get()=_spotifyAppRemote

    /*private val _recommendMusicState = MutableStateFlow<GetRecommendMusicState>(GetRecommendMusicState.Loading)
    val recommendMusicState:StateFlow<GetRecommendMusicState> get() = _recommendMusicState.asStateFlow()*/

    private val _recommendMusicList = MutableLiveData<ResponseRecommendMusicDto>()
    val recommendMusicList: MutableLiveData<ResponseRecommendMusicDto> get() = _recommendMusicList

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

   /* init {
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
    }*/

    fun setAccessToken(token:String){
        _accessToken.value=token
    }

    fun getRecommendMusic(lat: String, lng: String, musicMode: String) {
        Log.d("playviewmodel", "getRecommendMusic - ${accessToken.value!!}, ${lat}, ${lng}, ${musicMode}, ${isFirst}")
        viewModelScope.launch {
            authRepository.getRecommendMusic(accessToken.value!!, lat, lng, musicMode, isFirst++).onSuccess { response ->
                _recommendMusicList.value = response
                Log.d("playViewmodel", "get recommend music success\n${response.lists}")
            }.onFailure { throwable ->
                //_recommendMusicState.value =
                GetRecommendMusicState.Error("Error response failure: ${throwable.message}")
                Log.e("playViewmodel", "Error: ${throwable.message}")
                Log.e("playViewmodel", Log.getStackTraceString(throwable))

                if (throwable is HttpException) {
                    try {
                        val errorBody: ResponseBody? = throwable.response()?.errorBody()
                        val errorBodyString = errorBody?.string() ?: ""

                        // 예외 처리 코드
                        try {
                            // JSONObject를 사용하여 메시지 추출
                            val jsonObject = JSONObject(errorBodyString)
                            val errorMessage = jsonObject.optString("errMsg", "Unknown error")

                            // 추출된 에러 메시지 로깅
                            Log.e("allviewmodel", "Error message: $errorMessage")
                        } catch (jsonException: JSONException) {
                            // JSON 파싱 실패 시 로깅
                            Log.e("allviewmodel", "Error parsing JSON response", jsonException)
                        }

                    } catch (e: Exception) {
                        // errorBodyString을 가져오는 중에 발생할 수 있는 다른 예외 처리
                        Log.e("allviewmodel", "Error parsing error body", e)
                    }
                }
            }
        }
        if (isFirst == 3) {
            isFirst = 2
        }
    }

    fun recommendMusicListReset(){
        _recommendMusicList.value = _recommendMusicList.value?.copy(lists = emptyList())
        isFirst=1
    }


    fun loginSpotify() {
        _isLoginSpotify.value = true
    }

    fun getFavoriteMusicList() {
        viewModelScope.launch {
            authRepository.getFavoriteMusic(accessToken.value!!).onSuccess { response ->
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
            authRepository.addFavoriteMusic(accessToken.value!!, playerState.track.uri, playerState.track.name,
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
            authRepository.deleteFavoriteMusic(accessToken.value!!,trackId).onSuccess { response->
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