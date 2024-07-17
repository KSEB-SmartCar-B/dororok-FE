package com.kseb.smart_car.presentation.join

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class JoinViewModel: ViewModel() {
    val userList: MutableList<Join> = mutableListOf()
}