package com.kseb.smart_car.presentation.main.my.music

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kseb.smart_car.databinding.ItemDeletedmusicBinding
import com.kseb.smart_car.databinding.ItemDeletedplaceBinding

class DeletedmusicAdapter(): RecyclerView.Adapter<DeletedmusicAdapter.DeletedmusicViewHolder>() {

    private val musicList = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",)

    inner class DeletedmusicViewHolder(
        private val binding: ItemDeletedmusicBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: String) {
            binding.tvTitle.text = musicList[position]

            binding.btnPlay.setVisibility(View.GONE);

            clickItem()
        }

        private fun clickItem() {
            binding.musicGray.setOnClickListener {
                binding.musicGray.isSelected = !binding.musicGray.isSelected
                binding.ivCircle.isSelected = !binding.ivCircle.isSelected
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeletedmusicViewHolder {
        val binding = ItemDeletedmusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeletedmusicViewHolder(binding)
    }

    override fun getItemCount(): Int = musicList.size

    override fun onBindViewHolder(holder: DeletedmusicViewHolder, position: Int) {
        val item = musicList[position]
        holder.onBind(item)
    }
}