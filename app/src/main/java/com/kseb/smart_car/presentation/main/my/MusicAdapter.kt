package com.kseb.smart_car.presentation.main.my

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kseb.smart_car.databinding.ItemMusicBinding

class MusicAdapter(): RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    private val musicList = listOf("롸잇나우", "슈퍼내추럴", "디토", "핫스위트", "하입보이", "어텐션", "갓", "오마갓", "수퍼샤이", "뉴진스",)

    inner class MusicViewHolder(
        private val binding: ItemMusicBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: String) {
            binding.tvSong.text = musicList[position]
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val binding = ItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MusicViewHolder(binding)
    }

    override fun getItemCount(): Int = musicList.size

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val item = musicList[position]
        holder.onBind(item)
    }
}