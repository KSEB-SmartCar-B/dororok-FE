package com.kseb.smart_car.presentation.main.place.placeDetail

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.kseb.smart_car.data.responseDto.ResponseFavoritePlaceDto
import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceNearbyDetailDto
import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceNearbyDto
import com.kseb.smart_car.databinding.ItemPlaceDetailBinding

class PlaceDetailAdapter(
    private val onPlaceSave: (ResponseRecommendPlaceNearbyDto.PlaceList) -> Unit,
    private val clickPlace:(ResponseRecommendPlaceNearbyDto.PlaceList, View, /*View, AppCompatButton*/)->Unit
):RecyclerView.Adapter<PlaceDetailAdapter.PlaceDetailViewHolder>() {
    private val placeList = mutableListOf<ResponseRecommendPlaceNearbyDto.PlaceList>()
    private val savedPlaceList = mutableListOf<ResponseFavoritePlaceDto.FavoritesPlaceListDto>()

    inner class PlaceDetailViewHolder(
        private val binding:ItemPlaceDetailBinding
    ):RecyclerView.ViewHolder(binding.root){
        fun onBind(item:ResponseRecommendPlaceNearbyDto.PlaceList){
            binding.ivPhoto.load(item.imageUrl)
            binding.tvMainplace.text=item.title

            // savedPlaceList에 해당 아이템이 있는지 확인
            val isSaved = savedPlaceList.any { it.contentId == item.contentId }
            /*binding.btnSaved.isSelected = isSaved*/

            itemView.setOnClickListener {
                clickPlace(item, binding.ivPhoto, /*binding.ivCircle, binding.btnSaved*/)  // 클릭 시 place와 view를 전달
            }
            //clickSaveButton(item)
        }

       /* private fun clickSaveButton(item: ResponseRecommendPlaceNearbyDto.PlaceList) {
            binding.btnSaved.setOnClickListener {
                binding.btnSaved.isSelected = !binding.btnSaved.isSelected
                onPlaceSave(item)
            }
        }*/

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceDetailViewHolder {
        val binding=ItemPlaceDetailBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PlaceDetailViewHolder(binding)
    }

    override fun getItemCount(): Int =placeList.size

    override fun onBindViewHolder(holder: PlaceDetailViewHolder, position: Int) {
        val item=placeList[position]
        holder.onBind(item)
    }

    fun getNearbyPlaceList(list:List<ResponseRecommendPlaceNearbyDto.PlaceList>){
        placeList.addAll(list)
        Log.d("placeAdapter","placelist: ${placeList}")
        notifyDataSetChanged()
    }

    fun getSavedPlace(list: List<ResponseFavoritePlaceDto.FavoritesPlaceListDto>) {
        savedPlaceList.clear() // 기존 리스트를 지우고
        savedPlaceList.addAll(list)
        notifyDataSetChanged()
    }
}