package com.kseb.smart_car.presentation.main.my.music

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.kseb.smart_car.R
import com.kseb.smart_car.data.responseDto.ResponseFavoriteMusicDto
import com.kseb.smart_car.data.responseDto.ResponseMusicDto
import com.kseb.smart_car.databinding.ItemSavedmusicBinding

class SavedMusicAdapter(
    private val clickPlayPauseButton: (String) -> Unit,
    private val deleteMusicListChanged: (List<String>) -> Unit
) :
    RecyclerView.Adapter<SavedMusicAdapter.SavedMusicViewHolder>() {

    //private val musicList = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",)
    private val favoriteMusicList = mutableListOf<ResponseFavoriteMusicDto.FavoriteMusicListDto>()
    private val deleteMusicList = mutableListOf<String>()
    private var currentlyPlayingTrackId: String? = null
    private var isEditMode = false

    inner class SavedMusicViewHolder(
        private val binding: ItemSavedmusicBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(
            item: ResponseFavoriteMusicDto.FavoriteMusicListDto,
            clickPlayPauseButton: (String) -> Unit
        ) {
            with(binding) {
                val imageUrl = "https://i.scdn.co/image/${item.imageUrl}"
                ivMusic.load(imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.dororok_logo) // 로딩 중 표시할 이미지
                    transformations(
                        RoundedCornersTransformation(15f)
                    )
                    size(200, 200)
                    scale(Scale.FILL)
                }
                tvTitle.text = item.title
                tvSinger.text = item.artist

                // 재생 중인 노래인지 확인하여 아이콘 업데이트
                if (item.trackId == currentlyPlayingTrackId) {
                    btnPlay.setImageResource(R.drawable.btn_pause_yellow)
                } else {
                    btnPlay.setImageResource(R.drawable.btn_play_yellow)
                }

                playPauseButton(item, clickPlayPauseButton)

                // editClick 상태에 따라 musicGray 뷰의 가시성을 제어합니다.
                musicGray.visibility = if (isEditMode) View.VISIBLE else View.INVISIBLE
                btnPlay.visibility = if (isEditMode) View.INVISIBLE else View.VISIBLE
                ivCircle.visibility = if (isEditMode) View.VISIBLE else View.INVISIBLE

                itemView.takeIf { ivCircle.visibility == View.VISIBLE }?.setOnClickListener {
                    ivCircle.isSelected = !ivCircle.isSelected
                    musicGray.isSelected=!musicGray.isSelected
                    deleteMusicList.apply {
                        val trackId = item.trackId
                        if (trackId in this) remove(trackId) else add(trackId)
                    }
                }
            }
        }

        private fun playPauseButton(
            item: ResponseFavoriteMusicDto.FavoriteMusicListDto,
            clickPlayPauseButton: (String) -> Unit
        ) {
            binding.btnPlay.setOnClickListener {
                Log.d("savedMusicAdapter", "btnPlay click!")
                if (item.trackId == currentlyPlayingTrackId) {
                    // 현재 재생 중인 트랙을 일시 정지
                    currentlyPlayingTrackId = null
                    notifyItemChanged(adapterPosition)
                } else {
                    // 새로운 트랙을 재생
                    val previousPlayingPosition =
                        favoriteMusicList.indexOfFirst { it.trackId == currentlyPlayingTrackId }
                    currentlyPlayingTrackId = item.trackId
                    notifyItemChanged(previousPlayingPosition) // 이전 트랙 일시 정지 상태로 변경
                    notifyItemChanged(adapterPosition) // 현재 트랙 재생 상태로 변경
                    Log.d("savedMusicAdapter", "${item.title} play~")
                }
                clickPlayPauseButton(item.trackId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedMusicViewHolder {
        val binding =
            ItemSavedmusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SavedMusicViewHolder(binding)
    }

    override fun getItemCount(): Int = favoriteMusicList.size

    override fun onBindViewHolder(holder: SavedMusicViewHolder, position: Int) {
        val item = favoriteMusicList[position]
        holder.onBind(item, clickPlayPauseButton)
    }

    fun setMusicList(list: List<ResponseFavoriteMusicDto.FavoriteMusicListDto>) {
        favoriteMusicList.addAll(list)
        notifyDataSetChanged()
    }

    fun toggleEditMode(isEdit: Boolean) {
        isEditMode = isEdit

        if (!isEditMode) {
            // deleteMusicList에 있는 항목들을 favoriteMusicList에서 제거하면서 애니메이션을 적용
            val positionsToRemove = favoriteMusicList.mapIndexedNotNull { index, item ->
                if (item.trackId in deleteMusicList) index else null
            }

            positionsToRemove.sortedDescending().forEach { position ->
                favoriteMusicList.removeAt(position)
                notifyItemRemoved(position) // 각 항목을 삭제하면서 애니메이션 적용
            }

            deleteMusicListChanged(deleteMusicList.toList())
            deleteMusicList.clear() // 콜백 호출 후 리스트 초기화

            // remaining items' musicGray 상태 업데이트
            favoriteMusicList.forEachIndexed { index, _ ->
                notifyItemChanged(index)
            }
        } else {
            notifyDataSetChanged() // Edit 모드가 활성화될 때는 전체 데이터 갱신
        }
    }

    // ItemTouchHelper를 설정하는 메서드
    fun attachToRecyclerView(recyclerView: RecyclerView) {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val trackId = favoriteMusicList[position].trackId

                // deleteMusicList와 favoriteMusicList에서 해당 아이템을 제거
                deleteMusicList.add(trackId)
                favoriteMusicList.removeAt(position)
                notifyItemRemoved(position)

                // 필요시 이때 콜백 호출 (Edit 모드가 아닐 경우 즉시 적용)
                if (!isEditMode) {
                    deleteMusicListChanged(deleteMusicList.toList())
                    deleteMusicList.clear()
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}