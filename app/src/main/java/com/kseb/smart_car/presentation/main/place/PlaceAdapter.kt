package com.kseb.smart_car.presentation.main.place

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.kseb.smart_car.data.responseDto.ResponseFavoritePlaceDto
import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceNearbyDto
import com.kseb.smart_car.data.responseDto.ResponseSaveFavoritePlaceDto
import com.kseb.smart_car.databinding.ItemPlaceBinding

class PlaceAdapter(
    context: Context,
    private val where: String,
    private val onPlaceSave:(ResponseRecommendPlaceNearbyDto.PlaceList)->Unit,
    private val clickPlace:(ResponseRecommendPlaceNearbyDto.PlaceList, View)->Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflater by lazy { LayoutInflater.from(context) }
    private val placeList = listOf("1", "2", "3", "4", "5", "6")
    private val placeNearbyList = mutableListOf<ResponseRecommendPlaceNearbyDto.PlaceList>()
    private val savedPlaceList = mutableListOf<ResponseFavoritePlaceDto.FavoritesPlaceListDto>()

    inner class PlaceNearbyViewHolder(
        private val binding: ItemPlaceBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: ResponseRecommendPlaceNearbyDto.PlaceList) {
            binding.ivPhoto.load(item.imageUrl)
            binding.tvMainplace.text = item.title
            binding.tvSubplace.text = item.address

            // savedPlaceList에 해당 아이템이 있는지 확인
            val isSaved = savedPlaceList.any { it.contentId == item.contentId }
            binding.ivSaved.isSelected = isSaved

            clickSaveButton(item)
            // 바인딩 로직
            itemView.setOnClickListener {
                clickPlace(item, binding.ivPhoto)  // 클릭 시 place와 view를 전달
            }
        }

        private fun clickSaveButton(item: ResponseRecommendPlaceNearbyDto.PlaceList) {
            binding.ivSaved.setOnClickListener {
                binding.ivSaved.isSelected = !binding.ivSaved.isSelected
                onPlaceSave(item)
            }
        }
    }

    inner class PlaceViewHolder(
        private val binding: ItemPlaceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: String) {
            binding.tvMainplace.text = item

            clickSaveButton()
        }

        private fun clickSaveButton() {
            binding.ivSaved.isSelected = false
            binding.ivSaved.setOnClickListener {
                binding.ivSaved.isSelected = !binding.ivSaved.isSelected
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (where) {
            "nearby" -> PlaceNearbyViewHolder(ItemPlaceBinding.inflate(inflater, parent, false))
            else -> PlaceViewHolder(ItemPlaceBinding.inflate(inflater, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return when (where) {
            "nearby" -> placeNearbyList.size
            else -> placeList.size
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PlaceNearbyViewHolder -> {
                val itemNearby = placeNearbyList[position]
                holder.onBind(itemNearby)
            }
            is PlaceViewHolder -> {
                val itemPlace = placeList[position]
                holder.onBind(itemPlace)
            }
        }
    }

    fun getNearbyPlaceList(list: List<ResponseRecommendPlaceNearbyDto.PlaceList>) {
        placeNearbyList.addAll(list)
        Log.d("placeAdapter", "placelist: ${placeNearbyList}")
        notifyDataSetChanged()
    }

    fun getSavedPlace(list:List<ResponseFavoritePlaceDto.FavoritesPlaceListDto>){
        savedPlaceList.addAll(list)
        notifyDataSetChanged()
    }
}