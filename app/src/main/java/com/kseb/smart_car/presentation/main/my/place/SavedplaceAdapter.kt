package com.kseb.smart_car.presentation.main.my.place

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kseb.smart_car.databinding.ItemSavedplaceBinding

class SavedplaceAdapter(): RecyclerView.Adapter<SavedplaceAdapter.SavedplaceViewHolder>() {

    private val placeList = listOf("1", "2", "3", "4", "5", "6", "7", "8",)

    inner class SavedplaceViewHolder(
        private val binding: ItemSavedplaceBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: String) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedplaceViewHolder {
        val binding = ItemSavedplaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SavedplaceViewHolder(binding)
    }

    override fun getItemCount(): Int = placeList.size

    override fun onBindViewHolder(holder: SavedplaceViewHolder, position: Int) {
        val item = placeList[position]
        holder.onBind(item)
    }
}