package com.kseb.smart_car.presentation.main.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kseb.smart_car.databinding.ItemSearchBinding

class SearchAdapter() : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {
    private val searchList: MutableList<String> = mutableListOf()

    inner class SearchViewHolder(
        private val binding: ItemSearchBinding,
        private val adapter: SearchAdapter
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            // Delete 버튼 클릭 리스너 설정
            binding.tvDelete.setOnClickListener {
                adapter.removeItem(adapterPosition)
            }
        }

        fun onBind(item: String) {
            binding.tvSearch.text = searchList[position]
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding = ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchViewHolder(binding, this)
    }

    override fun getItemCount(): Int = searchList.size

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val item = searchList[position]
        holder.onBind(item)
    }

    fun getList(text: String) {
        if (text in searchList) {
            searchList.remove(text)
            searchList.add(0, text)
        } else searchList.add(0, text)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < searchList.size) {
            searchList.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}