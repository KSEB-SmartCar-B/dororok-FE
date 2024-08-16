package com.kseb.smart_car.presentation.main.my.place

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.kseb.smart_car.R
import com.kseb.smart_car.data.responseDto.ResponseFavoritePlaceDto
import com.kseb.smart_car.databinding.ItemSavedplaceBinding

class SavedPlaceAdapter(): RecyclerView.Adapter<SavedPlaceAdapter.SavedPlaceViewHolder>() {

    private val placeList = mutableListOf<ResponseFavoritePlaceDto.FavoritesPlaceListDto>()

    inner class SavedPlaceViewHolder(
        private val binding: ItemSavedplaceBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: ResponseFavoritePlaceDto.FavoritesPlaceListDto) {
            binding.ivPlace.load(item.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.dororok_logo) // 로딩 중 표시할 이미지
                size(300, 300)
                scale(Scale.FILL)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedPlaceViewHolder {
        val binding = ItemSavedplaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SavedPlaceViewHolder(binding)
    }

    override fun getItemCount(): Int = placeList.size

    override fun onBindViewHolder(holder: SavedPlaceViewHolder, position: Int) {
        val item = placeList[position]
        holder.onBind(item)
    }

    fun setList(list:List<ResponseFavoritePlaceDto.FavoritesPlaceListDto>){
        placeList.addAll(list)
        notifyDataSetChanged()
    }
}