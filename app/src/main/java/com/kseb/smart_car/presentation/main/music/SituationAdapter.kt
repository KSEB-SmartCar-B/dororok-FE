package com.kseb.smart_car.presentation.main.music

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.kseb.smart_car.R
import com.kseb.smart_car.data.responseDto.ResponseSituationDto
import com.kseb.smart_car.databinding.ItemGenreBinding
import com.kseb.smart_car.databinding.ItemNaviSituationBinding
import com.kseb.smart_car.databinding.ItemSituationBinding

class SituationAdapter(
    context: Context,
    private val itemClickListener: (String) -> Unit,
    private val where: String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val inflater by lazy { LayoutInflater.from(context) }
    private val situationList = mutableListOf<ResponseSituationDto.SituationList>()

    inner class MusicSituationViewHolder(
        private val binding: ItemSituationBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(situation: ResponseSituationDto.SituationList) {
            binding.tvSituation.text = situation.name
            binding.ivSituation.load(situation.imageUrl)

            binding.ivSituation.setOnClickListener {
                itemClickListener(situation.name)
            }
        }
    }

    inner class NaviSituationViewHolder(
        private val binding: ItemNaviSituationBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(situation: ResponseSituationDto.SituationList) {
            binding.btnSubject.text = situation.name

            binding.btnSubject.setOnClickListener {
                itemClickListener(situation.name)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(where){
            "music" ->
                MusicSituationViewHolder(ItemSituationBinding.inflate(inflater,parent,false))
            else ->  NaviSituationViewHolder(ItemNaviSituationBinding.inflate(inflater,parent, false))
        }
    }

    override fun getItemCount(): Int = situationList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = situationList[position]
        when(holder){
            is MusicSituationViewHolder->holder.onBind(item)
            is NaviSituationViewHolder->holder.onBind(item)
        }
    }

    fun getList(list: List<ResponseSituationDto.SituationList>) {
//        situationList.clear()
        situationList.addAll(list)
        notifyDataSetChanged()
    }
}