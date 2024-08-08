package com.kseb.smart_car.presentation.main.music

import androidx.lifecycle.ViewModel

class SituationViewModel: ViewModel() {
    fun makeList():List<String>{
        return listOf(
            "도로록 Pick!",
            "일상",
            "출근",
            "퇴근",
            "여행",
            "드라이브",
            "데이트",
            "친구들과",
        )
    }
}