package com.kseb.smart_car.presentation.main

import androidx.lifecycle.ViewModel

class SubjectViewModel: ViewModel() {
    fun makeList():List<String>{
        return listOf(
            "식당",
            "주차장",
            "관광명소",
            "주정차금지구역",
        )
    }
}