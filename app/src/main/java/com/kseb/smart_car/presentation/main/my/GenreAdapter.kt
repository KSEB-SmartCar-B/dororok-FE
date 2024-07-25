package com.kseb.smart_car.presentation.main.my

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.ItemGenreBinding
import com.kseb.smart_car.presentation.join.Genre
/*import com.kseb.smart_car.presentation.join.Join2Adapter
import com.kseb.smart_car.presentation.join.JoinViewModel*/

class GenreAdapter(private val onButtonClick: (String) -> Unit): RecyclerView.Adapter<GenreAdapter.GenreViewHolder>() {
    private val genreList = mutableListOf<Genre>()

    inner class GenreViewHolder(
        private val binding: ItemGenreBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(genre: Genre, onButtonClick: (String) -> Unit) {
            binding.tvGenre.text = genre.name
            binding.ivGenre.setImageResource(genre.photo?: R.drawable.genre_dance)
            //setting()
            genreButton(onButtonClick)
        }

        private fun genreButton(onButtonClick: (String) -> Unit) {
            val ivGenre = binding.ivGenre
            val tvGenre = binding.tvGenre

            ivGenre.isSelected = false

            ivGenre.setOnClickListener {
                ivGenre.isSelected = !ivGenre.isSelected
                val overlay = binding.colorOverlay
                if (overlay.visibility == View.GONE) {
                    overlay.visibility = View.VISIBLE
                } else {
                    overlay.visibility = View.GONE
                }
                onButtonClick(tvGenre.text.toString())
            }
        }

       /* private fun setting() {
            val genre = viewModel.genre
            val btnGenre = binding.btnGenre
            for (text in genre) {
                if (btnGenre.text == text) {
                    btnGenre.isSelected = true
                }
            }
        }*/
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val binding = ItemGenreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GenreViewHolder(binding)
    }

    override fun getItemCount(): Int = genreList.size

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        val item = genreList[position]
        holder.onBind(item, onButtonClick)
    }

    fun getList(list: List<Genre>) {
        genreList.addAll(list)
        notifyDataSetChanged()
    }
}