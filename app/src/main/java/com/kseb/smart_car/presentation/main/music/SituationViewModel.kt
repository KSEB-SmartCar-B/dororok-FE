package com.kseb.smart_car.presentation.main.music

import androidx.lifecycle.ViewModel

class SituationViewModel: ViewModel() {
    fun makeList():List<String>{
        return listOf(
            "일상",
            "출근",
            "퇴근",
            "여행",
            "드라이브",
            "도로록 Pick!",
        )
    }
}