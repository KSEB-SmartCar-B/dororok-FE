package com.kseb.smart_car.presentation.main

import androidx.recyclerview.widget.RecyclerView
import com.kseb.smart_car.databinding.ItemSubjectBinding

class SubjectViewHolder(
    private val binding:ItemSubjectBinding
):RecyclerView.ViewHolder(binding.root) {
    fun onBind(item:String){
        binding.btnSubject.text=item
    }
}