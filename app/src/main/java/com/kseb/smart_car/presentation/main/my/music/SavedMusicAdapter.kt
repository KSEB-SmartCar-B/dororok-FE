package com.kseb.smart_car.presentation.main.my.music

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.kseb.smart_car.R
import com.kseb.smart_car.data.responseDto.ResponseFavoriteMusicDto
import com.kseb.smart_car.databinding.ItemSavedmusicBinding

class SavedMusicAdapter(private val clickPlayPauseButton:(String)->Unit) : RecyclerView.Adapter<SavedMusicAdapter.SavedMusicViewHolder>() {

    //private val musicList = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",)
    private val favoriteMusicList = mutableListOf<ResponseFavoriteMusicDto.FavoriteMusicListDto>()
    private var currentlyPlayingTrackId: String? = null

    inner class SavedMusicViewHolder(
        private val binding: ItemSavedmusicBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: ResponseFavoriteMusicDto.FavoriteMusicListDto, clickPlayPauseButton: (String) -> Unit) {
            with(binding) {
                val imageUrl = "https://i.scdn.co/image/${item.imageUrl}"
                ivMusic.load(imageUrl)
                tvTitle.text = item.title
                tvSinger.text = item.artist

                // 재생 중인 노래인지 확인하여 아이콘 업데이트
                if (item.trackId == currentlyPlayingTrackId) {
                    btnPlay.setImageResource(R.drawable.btn_pause_yellow)
                } else {
                    btnPlay.setImageResource(R.drawable.btn_play_yellow)
                }

                playPauseButton(item) { clickPlayPauseButton }
            }
        }
        private fun playPauseButton(item:ResponseFavoriteMusicDto.FavoriteMusicListDto,clickPlayPauseButton: (String) -> Unit){
            binding.btnPlay.setOnClickListener {
                if (item.trackId == currentlyPlayingTrackId) {
                    // 현재 재생 중인 트랙을 일시 정지
                    currentlyPlayingTrackId = null
                    notifyItemChanged(adapterPosition)
                } else {
                    // 새로운 트랙을 재생
                    val previousPlayingPosition = favoriteMusicList.indexOfFirst { it.trackId == currentlyPlayingTrackId }
                    currentlyPlayingTrackId = item.trackId
                    notifyItemChanged(previousPlayingPosition) // 이전 트랙 일시 정지 상태로 변경
                    notifyItemChanged(adapterPosition) // 현재 트랙 재생 상태로 변경

                    clickPlayPauseButton(item.trackId)
                }
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
        holder.onBind(item,clickPlayPauseButton)
    }

    fun setMusicList(list: List<ResponseFavoriteMusicDto.FavoriteMusicListDto>) {
        favoriteMusicList.addAll(list)
        notifyDataSetChanged()
    }
}