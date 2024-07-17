package com.kseb.smart_car.presentation.join

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.ItemGenreBinding

class Join2Adapter(): RecyclerView.Adapter<Join2ViewHolder>() {
    private val genreList = mutableListOf<Join2>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Join2ViewHolder {
        val binding = ItemGenreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Join2ViewHolder(binding)
    }

    override fun getItemCount(): Int = genreList.size

    override fun onBindViewHolder(holder: Join2ViewHolder, position: Int) {
        val item = genreList[position]
        holder.onBind(item)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getList(list: List<Join2>) {
        genreList.addAll(list)
        notifyDataSetChanged()
    }
}