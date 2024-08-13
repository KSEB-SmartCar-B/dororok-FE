package com.kseb.smart_car.presentation.main.my.place

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.kseb.smart_car.data.responseDto.ResponseFavoritePlaceDto
import com.kseb.smart_car.databinding.ItemSavedplaceBinding
import okhttp3.internal.notify

class SavedplaceAdapter(): RecyclerView.Adapter<SavedplaceAdapter.SavedplaceViewHolder>() {

    private val placeList = mutableListOf<ResponseFavoritePlaceDto.FavoritesPlaceListDto>()

    inner class SavedplaceViewHolder(
        private val binding: ItemSavedplaceBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: ResponseFavoritePlaceDto.FavoritesPlaceListDto) {
            binding.ivPlace.load(item.imageUrl)
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

    fun setList(list:List<ResponseFavoritePlaceDto.FavoritesPlaceListDto>){
        placeList.addAll(list)
        notifyDataSetChanged()
    }
}