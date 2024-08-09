package com.kseb.smart_car.presentation.main.my.music

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kseb.smart_car.databinding.ItemSavedmusicBinding

class SavedmusicAdapter(): RecyclerView.Adapter<SavedmusicAdapter.SavedmusicViewHolder>() {

    private val musicList = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",)

    inner class SavedmusicViewHolder(
        private val binding: ItemSavedmusicBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: String) {
            binding.tvTitle.text = musicList[position]
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedmusicViewHolder {
        val binding = ItemSavedmusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SavedmusicViewHolder(binding)
    }

    override fun getItemCount(): Int = musicList.size

    override fun onBindViewHolder(holder: SavedmusicViewHolder, position: Int) {
        val item = musicList[position]
        holder.onBind(item)
    }
}