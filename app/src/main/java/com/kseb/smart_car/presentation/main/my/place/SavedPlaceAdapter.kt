package com.kseb.smart_car.presentation.main.my.place

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.kseb.smart_car.R
import com.kseb.smart_car.data.responseDto.ResponseFavoritePlaceDto
import com.kseb.smart_car.databinding.ItemSavedplaceBinding

class SavedPlaceAdapter(
    private val clickPlace: (ResponseFavoritePlaceDto.FavoritesPlaceListDto, View) -> Unit,
    private val deletePlaceListChanged: (List<String>) -> Unit
) : RecyclerView.Adapter<SavedPlaceAdapter.SavedPlaceViewHolder>() {

    private val favoritePlaceList = mutableListOf<ResponseFavoritePlaceDto.FavoritesPlaceListDto>()
    private val deletePlaceList = mutableListOf<String>()
    private var isEditMode = false

    inner class SavedPlaceViewHolder(
        private val binding: ItemSavedplaceBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: ResponseFavoritePlaceDto.FavoritesPlaceListDto) {
            with(binding) {
                ivPlace.load(item.imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.dororok_logo) // 로딩 중 표시할 이미지
                    size(300, 300)
                    scale(Scale.FILL)
                }
                placeGray.visibility = if (isEditMode) View.VISIBLE else View.INVISIBLE
                ivCircle.visibility = if (isEditMode) View.VISIBLE else View.INVISIBLE

                itemView.setOnClickListener {
                    if (ivCircle.visibility == View.VISIBLE) {
                        ivCircle.isSelected = !ivCircle.isSelected
                        deletePlaceList.apply {
                            if (item.contentId in this) remove(item.contentId) else add(item.contentId)
                        }
                    } else {
                        clickPlace(item, ivPlace)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedPlaceViewHolder {
        val binding =
            ItemSavedplaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SavedPlaceViewHolder(binding)
    }

    override fun getItemCount(): Int = favoritePlaceList.size

    override fun onBindViewHolder(holder: SavedPlaceViewHolder, position: Int) {
        val item = favoritePlaceList[position]
        holder.onBind(item)
    }

    fun setList(list: List<ResponseFavoritePlaceDto.FavoritesPlaceListDto>) {
        favoritePlaceList.addAll(list)
        notifyDataSetChanged()
    }

    fun toggleEditMode(isEdit: Boolean) {
        isEditMode = isEdit

        // Edit 모드가 종료될 때 deleteMusicList를 콜백으로 전달
        if (!isEditMode) {
            // favoriteMusicList에서 deleteMusicList에 있는 항목들 제거
            favoritePlaceList.removeAll { it.contentId.removePrefix("spotify:track:") in deletePlaceList }

            deletePlaceListChanged(deletePlaceList.toList())
            deletePlaceList.clear() // 콜백 호출 후 리스트 초기화
        }
        notifyDataSetChanged()
    }
}