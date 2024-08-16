package com.kseb.smart_car.presentation.main.place

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.kseb.smart_car.R
import com.kseb.smart_car.data.responseDto.ResponseFavoritePlaceDto
import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceNearbyDto
import com.kseb.smart_car.databinding.ItemPlaceBinding

class PlaceNearbyAdapter(
    private val onPlaceSave: (ResponseRecommendPlaceNearbyDto.PlaceList) -> Unit,
    private val clickPlace: (ResponseRecommendPlaceNearbyDto.PlaceList, View, View, AppCompatButton) -> Unit,
    private val goNavi:(ResponseRecommendPlaceNearbyDto.PlaceList)->Unit
):RecyclerView.Adapter<PlaceNearbyAdapter.PlaceViewHolder>() {
    private val placeList = mutableListOf<ResponseRecommendPlaceNearbyDto.PlaceList>()
    private val savedPlaceList = mutableListOf<ResponseFavoritePlaceDto.FavoritesPlaceListDto>()

    inner class PlaceViewHolder(
        private val binding: ItemPlaceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: ResponseRecommendPlaceNearbyDto.PlaceList) {
            binding.ivPhoto.load(item.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.dororok_loading) // 로딩 중 표시할 이미지
                transformations(RoundedCornersTransformation(10f, 10f, 0f, 0f)) // 필요한 경우 모서리 둥글게
                size(270, 170)
                scale(Scale.FILL)
            }
            binding.tvMainplace.text = item.title
            binding.tvSubplace.text = item.address

            // savedPlaceList에 해당 아이템이 있는지 확인
            val isSaved = savedPlaceList.any { it.contentId == item.contentId }
            binding.btnSaved.isSelected = isSaved

            clickSaveButton(item)
            // 바인딩 로직
            itemView.setOnClickListener {
                clickPlace(item, binding.ivPhoto, binding.ivCircle, binding.btnSaved)  // 클릭 시 place와 view를 전달
            }

            binding.ivNavigation.setOnClickListener{
                goNavi(item)
            }
        }

        private fun clickSaveButton(item: ResponseRecommendPlaceNearbyDto.PlaceList) {
            binding.btnSaved.setOnClickListener {
                binding.btnSaved.isSelected = !binding.btnSaved.isSelected
                onPlaceSave(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding=ItemPlaceBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PlaceViewHolder(binding)
    }

    override fun getItemCount(): Int = placeList.size

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val item=placeList[position]
        holder.onBind(item)
    }

    fun getNearbyPlaceList(list:List<ResponseRecommendPlaceNearbyDto.PlaceList>){
        val startPosition = placeList.size
        placeList.addAll(list)
        notifyItemRangeInserted(startPosition, list.size)
        Log.d("placeAdapter", "placelist: ${placeList}")
    }

    fun getSavedPlace(list: List<ResponseFavoritePlaceDto.FavoritesPlaceListDto>) {
        savedPlaceList.clear() // 기존 리스트를 지우고
        savedPlaceList.addAll(list)
        notifyDataSetChanged()
    }
}