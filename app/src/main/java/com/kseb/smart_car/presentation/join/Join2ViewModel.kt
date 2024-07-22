package com.kseb.smart_car.presentation.join

import androidx.lifecycle.ViewModel

class Join2ViewModel: ViewModel() {
    fun makeList(): List<String> {
        return listOf(
            "댄스",
            "발라드",
            "인디",
            "트로트",
            "OST",
            "POP",
            "J-POP",
            "재즈",
            "클래식",
            "뉴에이지",
            "월드뮤직",
            "일렉트로니카",
            "국내 록/메탈",
            "해외 록/메탈",
            "국내 랩/힙합",
            "해외 랩/힙합",
            "국내 R&B/Soul",
            "해외 R&B/Soul",
            "국내 포크/블루스",
            "해외 포크/블루스/컨트리")
    }
}