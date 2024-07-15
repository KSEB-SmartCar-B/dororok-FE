package com.kseb.smart_car.presentation.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kseb.smart_car.databinding.ItemSubjectBinding

class SubjectAdapter():RecyclerView.Adapter<SubjectViewHolder>() {
    private val subjectList = mutableListOf<String>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val binding=ItemSubjectBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return SubjectViewHolder(binding)
    }

    override fun getItemCount(): Int = subjectList.size

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val item=subjectList[position]
        holder.onBind(item)
    }

    fun getList(list:List<String>){
        subjectList.addAll(list)
        notifyDataSetChanged()
    }

}