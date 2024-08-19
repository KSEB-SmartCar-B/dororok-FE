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

    private val placeList = mutableListOf<ResponseFavoritePlaceDto.FavoritesPlaceListDto>()
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
                        if (ivCircle.isSelected) {
                            deletePlaceList.add(item.contentId)
                        } else {
                            deletePlaceList.remove(item.contentId)
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

    override fun getItemCount(): Int = placeList.size

    override fun onBindViewHolder(holder: SavedPlaceViewHolder, position: Int) {
        val item = placeList[position]
        holder.onBind(item)
    }

    fun setList(list: List<ResponseFavoritePlaceDto.FavoritesPlaceListDto>) {
        placeList.addAll(list)
        notifyDataSetChanged()
    }

    fun toggleEditMode(isEdit: Boolean) {
        isEditMode = isEdit
        if (!isEditMode) {
            // deletePlaceList에 있는 항목들을 placeList에서 제거하면서 애니메이션을 적용
            val positionsToRemove = placeList.mapIndexedNotNull { index, item ->
                if (item.contentId in deletePlaceList) index else null
            }

            positionsToRemove.sortedDescending().forEach { position ->
                placeList.removeAt(position)
                notifyItemRemoved(position) // 각 항목을 삭제하면서 애니메이션 적용
            }

            deletePlaceListChanged(deletePlaceList.toList())
            deletePlaceList.clear() // 콜백 호출 후 리스트 초기화

            // 남아 있는 항목들에 대해 UI 상태를 업데이트
            placeList.forEachIndexed { index, _ ->
                notifyItemChanged(index)
            }
        } else {
            notifyDataSetChanged() // Edit 모드가 활성화될 때는 전체 데이터 갱신
        }
    }
}