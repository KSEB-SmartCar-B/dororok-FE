package com.kseb.smart_car.presentation.join

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.ItemGenreBinding

class Join2Adapter(private val onButtonClick: (String) -> Unit): RecyclerView.Adapter<Join2Adapter.Join2ViewHolder>() {

    private val genreList = mutableListOf<String>()

    inner class Join2ViewHolder(
        private val binding: ItemGenreBinding

    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(genre: String, onButtonClick: (String) -> Unit) {
            binding.btnGenre.text = genre

            genreButton(onButtonClick)
        }

        private fun genreButton(onButtonClick: (String) -> Unit) {
            val btnGenre = binding.btnGenre

            btnGenre.isSelected = false

            btnGenre.setOnClickListener {
                btnGenre.isSelected = !btnGenre.isSelected
                onButtonClick(btnGenre.text.toString())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Join2ViewHolder {
        val binding = ItemGenreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Join2ViewHolder(binding)
    }

    override fun getItemCount(): Int = genreList.size

    override fun onBindViewHolder(holder: Join2ViewHolder, position: Int) {
        val item = genreList[position]
        holder.onBind(item, onButtonClick)
    }

    fun getList(list: List<String>) {
        genreList.addAll(list)
        notifyDataSetChanged()
    }
}