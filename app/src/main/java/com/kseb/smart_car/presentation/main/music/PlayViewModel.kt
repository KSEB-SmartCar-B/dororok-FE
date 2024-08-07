package com.kseb.smart_car.presentation.main.music

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kseb.smart_car.data.responseDto.ResponseMusicDto

class PlayViewModel:ViewModel() {
    var nextMusicList: List<ResponseMusicDto.MusicListDto> = mutableListOf()

    private val _isLoginSpotify = MutableLiveData<Boolean>(false)
    val isLoginSpotify:MutableLiveData<Boolean> get() = _isLoginSpotify

    init {
        setList()
    }

    private fun setList() {
        val list = listOf(
            ResponseMusicDto.MusicListDto("spotify:album:4m2880jivSbbyEGAKfITCa", 0),
            ResponseMusicDto.MusicListDto("spotify:artist:3WrFJ7ztbogyGnTHbHJFl2", 0),
            ResponseMusicDto.MusicListDto("spotify:album:4m2880jivSbbyEGAKfITCa", 0),
            ResponseMusicDto.MusicListDto("spotify:playlist:37i9dQZEVXbMDoHDwVN2tF", 0),
            ResponseMusicDto.MusicListDto("spotify:show:2tgPYIeGErjk6irHRhk9kj", 0)
        )
        nextMusicList = list
    }

    fun loginSpotify(){
        _isLoginSpotify.value=true
    }
}