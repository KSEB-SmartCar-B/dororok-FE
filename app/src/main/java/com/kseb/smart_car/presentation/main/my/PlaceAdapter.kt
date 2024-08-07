package com.kseb.smart_car.presentation.main.my

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kseb.smart_car.databinding.ItemPlaceBinding

class PlaceAdapter(): RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    private val placeList = listOf("수원시", "부산광역시", "서울특별시",)

    inner class PlaceViewHolder(
        private val binding: ItemPlaceBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: String) {
            binding.tvMainplace.text = placeList[position]
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceViewHolder(binding)
    }

    override fun getItemCount(): Int = placeList.size

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val item = placeList[position]
        holder.onBind(item)
    }
}