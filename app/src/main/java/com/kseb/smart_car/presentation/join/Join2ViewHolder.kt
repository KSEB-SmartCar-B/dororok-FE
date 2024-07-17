package com.kseb.smart_car.presentation.join

import androidx.recyclerview.widget.RecyclerView
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.ItemGenreBinding

class Join2ViewHolder(
    private val binding: ItemGenreBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun onBind(join2: Join2) {
        binding.btnGenre.text = join2.genre

        genreButton(join2)
    }

    private fun genreButton(join2: Join2) {
        val btnGenre = binding.btnGenre

        btnGenre.isSelected = false

        btnGenre.setOnClickListener {
            btnGenre.isSelected = !btnGenre.isSelected
            join2.isSelected = btnGenre.isSelected
        }
    }

}