//package com.kseb.smart_car.presentation.join
//
//import androidx.recyclerview.widget.RecyclerView
//import com.kseb.smart_car.R
//import com.kseb.smart_car.databinding.ItemGenreBinding
//
//class Join2ViewHolder(
//    private val binding: ItemGenreBinding
//
//) : RecyclerView.ViewHolder(binding.root) {
//    fun onBind(join2: Join2) {
//        binding.btnGenre.text = join2.genre
//
//        genreButton()
//    }
//
//    private fun genreButton() {
//        val btnGenre = binding.btnGenre
//
//        btnGenre.isSelected = false
//
//        btnGenre.setOnClickListener {
//            btnGenre.isSelected = !btnGenre.isSelected
//            val genre = btnGenre.text.toString()
//        }
//    }
//}