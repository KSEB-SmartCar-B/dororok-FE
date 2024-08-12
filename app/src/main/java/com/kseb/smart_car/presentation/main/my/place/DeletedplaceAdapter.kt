package com.kseb.smart_car.presentation.main.my.place

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kseb.smart_car.databinding.ItemDeletedplaceBinding

class DeletedplaceAdapter(): RecyclerView.Adapter<DeletedplaceAdapter.DeletedplaceViewHolder>() {

    private val placeList = listOf("1", "2", "3", "4", "5", "6", "7", "8",)

    inner class DeletedplaceViewHolder(
        private val binding: ItemDeletedplaceBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: String) {

            clickItem()
        }

        private fun clickItem() {
            binding.placeGray.setOnClickListener {
                binding.placeGray.isSelected = !binding.placeGray.isSelected
                binding.ivCircle.isSelected = !binding.ivCircle.isSelected
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeletedplaceViewHolder {
        val binding = ItemDeletedplaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeletedplaceViewHolder(binding)
    }

    override fun getItemCount(): Int = placeList.size

    override fun onBindViewHolder(holder: DeletedplaceViewHolder, position: Int) {
        val item = placeList[position]
        holder.onBind(item)
    }
}