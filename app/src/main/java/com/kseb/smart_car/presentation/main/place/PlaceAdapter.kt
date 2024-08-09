package com.kseb.smart_car.presentation.main.place

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kseb.smart_car.databinding.ItemMusicBinding
import com.kseb.smart_car.databinding.ItemPlaceBinding

class PlaceAdapter() : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    private val placeList = listOf("1", "2", "3", "4", "5", "6")

    inner class PlaceViewHolder(
        private val binding: ItemPlaceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: String) {
            binding.tvMainplace.text = placeList[position]

            clickSaveButton()
        }

        private fun clickSaveButton() {
            binding.ivSaved.isSelected = false
            binding.ivSaved.setOnClickListener {
                binding.ivSaved.isSelected = !binding.ivSaved.isSelected
            }
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