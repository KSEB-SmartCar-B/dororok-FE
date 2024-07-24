package com.kseb.smart_car.presentation.main.my

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.kseb.smart_car.databinding.ItemGenreBinding
import com.kseb.smart_car.presentation.join.Join2Adapter
import com.kseb.smart_car.presentation.join.JoinViewModel

class GenreAdapter(private val onButtonClick: (String) -> Unit): RecyclerView.Adapter<GenreAdapter.GenreViewHolder>() {
    private val genreList = mutableListOf<String>()

    inner class GenreViewHolder(
        private val binding: ItemGenreBinding,
        private val viewModel: MyViewModel
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(genre: String, onButtonClick: (String) -> Unit) {
            binding.btnGenre.text = genre

            setting()
            genreButton(onButtonClick)
        }

        private fun genreButton(onButtonClick: (String) -> Unit) {
            val btnGenre = binding.btnGenre

            btnGenre.setOnClickListener {
                btnGenre.isSelected = !btnGenre.isSelected
                onButtonClick(btnGenre.text.toString())
            }
        }

        private fun setting() {
            val genre = viewModel.genre
            val btnGenre = binding.btnGenre
            for (text in genre) {
                if (btnGenre.text == text) {
                    btnGenre.isSelected = true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val binding = ItemGenreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GenreViewHolder(binding, viewModel = MyViewModel())
    }

    override fun getItemCount(): Int = genreList.size

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        val item = genreList[position]
        holder.onBind(item, onButtonClick)
    }

    fun getList(list: List<String>) {
        genreList.addAll(list)
        notifyDataSetChanged()
    }
}