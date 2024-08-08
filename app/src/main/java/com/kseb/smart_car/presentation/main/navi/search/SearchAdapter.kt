package com.kseb.smart_car.presentation.main.navi.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kseb.smart_car.data.responseDto.ResponseSearchListDto
import com.kseb.smart_car.databinding.ItemSearchBinding

class SearchAdapter(private val deleteButtonClick: (String) -> Unit, private val clickButton:(String)->Unit) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {
    private val searchList: MutableList<String> = mutableListOf()

    inner class SearchViewHolder(
        private val binding: ItemSearchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: String) {
            binding.tvSearch.text = searchList[position]
            binding.ivDelete.setOnClickListener{
                deleteButtonClick(item)
            }
            binding.itemLayout.setOnClickListener{
                clickButton(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding = ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchViewHolder(binding)
    }

    override fun getItemCount(): Int = searchList.size

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val item = searchList[position]
        holder.onBind(item)
    }

    fun getList(text: ResponseSearchListDto) {
        searchList.clear()
        searchList.addAll(text.searchLogs)
        notifyDataSetChanged()
    }
}