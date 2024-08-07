package com.kseb.smart_car.presentation.main.navi.search

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kseb.smart_car.data.responseDto.ResponseAddressDto
import com.kseb.smart_car.data.responseDto.ResponseSearchListDto
import com.kseb.smart_car.databinding.ItemSearchBinding
import com.kseb.smart_car.databinding.ItemSearchListBinding

class SearchListAdapter(private val onButtonClick:(ResponseAddressDto.Document) -> Unit):RecyclerView.Adapter<SearchListAdapter.SearchListViewHolder>() {
    private val searchList:MutableList<ResponseAddressDto.Document> = mutableListOf()

    inner class SearchListViewHolder(
        private val binding:ItemSearchListBinding
    ):RecyclerView.ViewHolder(binding.root){
        fun onBind(item:ResponseAddressDto.Document){
            binding.tvPlace.text=item.placeName
            binding.tvAddress.text=item.roadAddressName
            binding.isl.setOnClickListener {
                onButtonClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchListViewHolder {
        val binding=ItemSearchListBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return SearchListViewHolder(binding)
    }

    override fun getItemCount(): Int=searchList.size

    override fun onBindViewHolder(holder: SearchListViewHolder, position: Int) {
        val item=searchList[position]
        holder.onBind(item)
    }

    fun getList(newList: List<ResponseAddressDto.Document>){
        searchList.clear()
        val limitedList = newList.take(20)
        searchList.addAll(limitedList)
        notifyDataSetChanged()
    }
}