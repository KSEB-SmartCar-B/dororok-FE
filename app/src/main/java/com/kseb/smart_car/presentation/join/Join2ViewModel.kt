package com.kseb.smart_car.presentation.join

import androidx.lifecycle.ViewModel

class Join2ViewModel: ViewModel() {
    fun makeList(): List<Join2> {
        return listOf(
            Join2("댄스"),
            Join2("국내 힙합"),
            Join2("팝"),
            Join2("국내 R&B"),
            Join2("JPOP"),
            Join2("클래식"),
            Join2("일렉트로닉"),
            Join2("펑크"),
            Join2("발라드"),
            Join2("트로트"),
            Join2("밴드"),
            Join2("어쿠스틱"),
            Join2("인디"),
            Join2("재즈"),
            Join2("컨트리"),
        )
    }
}