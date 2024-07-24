package com.kseb.smart_car.presentation.main.my

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyViewModel: ViewModel() {
    private val _buttonText = MutableLiveData<String>()
    val buttonText: LiveData<String> = _buttonText

    var gender = "여자"
    var nickname = "이지원"
    var birthYear = "1999"
    var birthMonth = "2"
    var birthDay = "14"
//    private var genre = ""

    fun getInfo(
        gender: String,
        nickname: String,
        birthYear: String,
        birthMonth: String,
        birthDay: String
    ) {
        this.gender = gender
        this.nickname = nickname
        this.birthYear = birthYear
        this.birthMonth = birthMonth
        this.birthDay= birthDay

        Log.d(
            "MyViewModel",
            "Gender: ${this.gender}, Nickname: ${this.nickname}, BirthYear: ${this.birthYear}, BirthMonth: ${this.birthMonth}, BirthDay: ${this.birthDay}"
        )
    }

}