package com.kseb.smart_car.presentation.main.my.music

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.kseb.smart_car.R
import com.kseb.smart_car.data.responseDto.ResponseFavoriteMusicDto
import com.kseb.smart_car.databinding.ItemSavedmusicBinding

class SavedMusicAdapter(private val clickPlayPauseButton: (String) -> Unit, private val clickMusic:(String)->Unit) :
    RecyclerView.Adapter<SavedMusicAdapter.SavedMusicViewHolder>() {

    //private val musicList = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",)
    private val favoriteMusicList = mutableListOf<ResponseFavoriteMusicDto.FavoriteMusicListDto>()
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
                    transformations(RoundedCornersTransformation(15f, 15f, 15f, 15f)) // 필요한 경우 모서리 둥글게
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
                btnPlay.visibility= if (isEditMode) View.INVISIBLE else View.VISIBLE
                ivCircle.visibility= if (isEditMode) View.VISIBLE else View.INVISIBLE

                if(ivCircle.visibility==View.VISIBLE){
                    itemView.setOnClickListener{
                        ivCircle.isSelected=!ivCircle.isSelected
                        clickMusic(item.trackId)
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
        notifyDataSetChanged()
    }
}