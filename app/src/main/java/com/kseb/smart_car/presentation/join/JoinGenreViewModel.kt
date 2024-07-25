package com.kseb.smart_car.presentation.join

import androidx.lifecycle.ViewModel
import com.kseb.smart_car.R

class JoinGenreViewModel: ViewModel() {
    fun makeList(): List<Genre> {
        return listOf(
            Genre("댄스", R.drawable.genre_dance),
            Genre("발라드", R.drawable.genre_balad),
            Genre("인디", R.drawable.genre_indi),
            Genre("트로트", R.drawable.genre_trot),
            Genre("OST", R.drawable.genre_ost),
            Genre("POP", R.drawable.genre_pop),
            Genre("J-POP", R.drawable.genre_jpop),
            Genre("재즈", R.drawable.genre_jazz),
            Genre("클래식", R.drawable.genre_classic),
            Genre("뉴에이지", R.drawable.genre_newage),
            Genre("일렉트로니카", R.drawable.genre_electronic),
            Genre("국내 밴드", R.drawable.genre_kband),
            Genre("해외 밴드", R.drawable.genre_band),
            Genre("국내 랩/힙합", R.drawable.genre_khiphop),
            Genre("해외 랩/힙합", R.drawable.genre_hiphop),
            Genre("국내 록/메탈", R.drawable.genre_krock),
            Genre("해외 록/메탈", R.drawable.genre_rock),
            Genre("국내 R&B/Soul", R.drawable.genre_krnb),
            Genre("해외 R&B/Soul", R.drawable.genre_rnb),
            Genre("국내 포크/블루스", R.drawable.genre_kfolk),
            Genre("해외 포크/블루스/컨트리", R.drawable.genre_folk),
        )
    }
}