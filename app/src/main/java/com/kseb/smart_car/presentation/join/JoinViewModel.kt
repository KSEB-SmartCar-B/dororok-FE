package com.kseb.smart_car.presentation.join

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDateTime

class JoinViewModel : ViewModel() {

    private val _buttonText = MutableLiveData<String>()
    val buttonText: LiveData<String> = _buttonText

    private val gender = MutableLiveData<String>()
    private val nickname = MutableLiveData<String>()
//    private val birthYear = MutableLiveData<String>()
//    private val birthMonth = MutableLiveData<String>()
//    private val birthDay = MutableLiveData<String>()
    private val birth = MutableLiveData<LocalDateTime>()
    private val genre = MutableLiveData<List<String>>()

    fun getInfo(
        gender: String,
        nickname: String,
        birthYear: String,
        birthMonth: String,
        birthDay: String
    ) {
        this.gender.value = gender
        this.nickname.value = nickname
        birth.value = createLocalDateTime(birthYear, birthMonth, birthDay)

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

    fun createLocalDateTime(birthYear: String, birthMonth: String, birthDay: String): LocalDateTime {
        val year = birthYear.toInt()
        val month = birthMonth.toInt()+1
        val day = birthDay.toInt()

        return LocalDateTime.of(year, month, day, 0, 0) // 시간을 00:00으로 설정
    }
}