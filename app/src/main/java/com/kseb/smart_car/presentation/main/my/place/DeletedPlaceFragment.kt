package com.kseb.smart_car.presentation.main.my.place

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kseb.smart_car.databinding.FragmentDeletedplaceBinding

class DeletedPlaceFragment: Fragment() {
    private var _binding: FragmentDeletedplaceBinding? = null
    private val binding: FragmentDeletedplaceBinding
        get() = requireNotNull(_binding) { "null" }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeletedplaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val deletedPlaceAdapter = DeletedPlaceAdapter()
        binding.rvPlace.adapter = deletedPlaceAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}