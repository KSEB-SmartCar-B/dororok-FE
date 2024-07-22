package com.kseb.smart_car.presentation.join

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class JoinViewModel : ViewModel() {

    private val _buttonText = MutableLiveData<String>()
    val buttonText: LiveData<String> = _buttonText

    private val gender = MutableLiveData<String>()
    private val nickname = MutableLiveData<String>()
    private val birthYear = MutableLiveData<String>()
    private val birthMonth = MutableLiveData<String>()
    private val birthDay = MutableLiveData<String>()
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
        this.birthYear.value = birthYear
        this.birthMonth.value = birthMonth
        this.birthDay.value = birthDay

        //잘 되는지 확인용
        Log.d(
            "JoinViewModel",
            "Gender: ${this.gender.value}, Nickname: ${this.nickname.value}, BirthYear: ${this.birthYear.value}, BirthMonth: ${this.birthMonth.value}, BirthDay: ${this.birthDay.value}"
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


}