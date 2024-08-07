package com.kseb.smart_car.presentation.main.music

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.kseb.smart_car.R
import com.kseb.smart_car.data.responseDto.ResponseMusicDto
import com.kseb.smart_car.databinding.ItemMusicBinding
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.Image

class PlayAdapter(private val context:Context, private val spotifyAppRemote: SpotifyAppRemote):RecyclerView.Adapter<PlayAdapter.PlayViewHolder>() {
    private val musicList = mutableListOf<ResponseMusicDto.MusicListDto>()

    inner class PlayViewHolder(
        private val binding:ItemMusicBinding,
    ):RecyclerView.ViewHolder(binding.root){
        fun onBind(music:ResponseMusicDto.MusicListDto){
            // URI로부터 이미지, 제목, 가수명을 가져와서 바인딩
            spotifyAppRemote.playerApi.play(music.trackUri).setResultCallback {
                spotifyAppRemote.playerApi.subscribeToPlayerState().setEventCallback { playerState ->
                    // 음악이 재생된 후 상태를 구독하여 정보를 가져옴
                    if (playerState.track.uri == music.trackUri) {
                        spotifyAppRemote.imagesApi.getImage(playerState.track.imageUri, Image.Dimension.LARGE)
                            .setResultCallback { bitmap ->
                                // Glide로 bitmap을 직접 설정하여 CenterCrop 및 RoundedCorners 적용
                                Glide.with(context)
                                    .load(bitmap)
                                    .transform(CenterCrop(), RoundedCorners(context.resources.getDimensionPixelSize(R.dimen.radius_music_image))) // 반지름을 dimens 파일에서 가져옴
                                    .into(binding.ivMusic)
                            }

                        binding.tvTitle.text = playerState.track.name
                        binding.tvSinger.text = playerState.track.artist.name
                        Log.d("PlayAdapter", "Title: ${playerState.track.name}")
                    }
                }
            }

            binding.root.setOnClickListener {
                Toast.makeText(context, "Play URI: ${music.trackUri}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayViewHolder {
        val binding=ItemMusicBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PlayViewHolder(binding)
    }

    override fun getItemCount(): Int  = musicList.size

    override fun onBindViewHolder(holder: PlayViewHolder, position: Int) {
        val item=musicList[position]
        holder.onBind(item)
    }

    fun getList(list:List<ResponseMusicDto.MusicListDto>){
        musicList.addAll(list)
        notifyDataSetChanged()
    }
}