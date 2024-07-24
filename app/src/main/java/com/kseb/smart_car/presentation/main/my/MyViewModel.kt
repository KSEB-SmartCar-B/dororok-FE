package com.kseb.smart_car.presentation.main.my

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDateTime

class MyViewModel: ViewModel() {
    private val _buttonText = MutableLiveData<String>()
    val buttonText: LiveData<String> = _buttonText

    var gender = "여자"
    var nickname = "이지원"
    var birthYear = "1999"
    var birthMonth = "2"
    var birthDay = "14"
    var birth: LocalDateTime? = null
    var genre = mutableListOf("댄스", "POP")

    fun getInfo(
        gender: String,
        nickname: String,
        birthYear: String,
        birthMonth: String,
        birthDay: String
    ) {
        this.gender = gender
        this.nickname = nickname
        birth = createLocalDateTime(birthYear, birthMonth, birthDay)


        Log.d(
            "MyViewModel",
            "Gender: ${this.gender}, Nickname: ${this.nickname}, Birth: ${this.birth}}"
        )
    }

    fun getGenre(text: String) {
        val currentList = genre ?: emptyList()
        val newList = currentList.toMutableList()
        if (text in newList) {
            newList.remove(text)
        } else newList.add(text)
        genre = newList

        //잘 되는지 확인용
        Log.d("MyViewModel", "Genre: ${this.genre}")
    }

    fun createLocalDateTime(birthYear: String, birthMonth: String, birthDay: String): LocalDateTime {
        val year = birthYear.toInt()
        val month = birthMonth.toInt()+1
        val day = birthDay.toInt()

        return LocalDateTime.of(year, month, day, 0, 0) // 시간을 00:00으로 설정
    }
}