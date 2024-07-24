package com.kseb.smart_car.presentation.main.music

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kseb.smart_car.databinding.ItemGenreBinding
import com.kseb.smart_car.databinding.ItemSituationBinding

class SituationAdapter(private val itemClickListener: (String) -> Unit):
    RecyclerView.Adapter<SituationAdapter.SituationViewHolder>() {

    private val situationList = mutableListOf<String>()

    inner class SituationViewHolder(
        private val binding: ItemSituationBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(situation: String) {
            binding.tvSituation.text = situation

            binding.ibSituation.setOnClickListener {
                itemClickListener(situation)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SituationViewHolder {
        val binding = ItemSituationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SituationViewHolder(binding)
    }

    override fun getItemCount(): Int = situationList.size

    override fun onBindViewHolder(holder: SituationViewHolder, position: Int) {
        val item = situationList[position]
        holder.onBind(item)
    }

    fun getList(list: List<String>) {
//        situationList.clear()
        situationList.addAll(list)
        notifyDataSetChanged()
    }
}