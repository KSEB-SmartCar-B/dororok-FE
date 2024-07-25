package com.kseb.smart_car.presentation.join

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.ItemGenreBinding

class JoinGenreAdapter(private val onButtonClick: (String) -> Unit): RecyclerView.Adapter<JoinGenreAdapter.Join2ViewHolder>() {

    private val genreList = mutableListOf<Genre>()

    inner class Join2ViewHolder(
        private val binding: ItemGenreBinding

    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(genre: Genre, onButtonClick: (String) -> Unit) {
            binding.tvGenre.text = genre.name

            val drawable = genre.photo ?: R.drawable.genre_dance
            binding.ivGenre.setImageResource(drawable)

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

    fun getList(list: List<Genre>) {
        genreList.addAll(list)
        notifyDataSetChanged()
    }
}
