package com.kseb.smart_car.presentation.main.my.music

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kseb.smart_car.databinding.FragmentDeletedmusicBinding
import com.kseb.smart_car.presentation.main.my.place.DeletedplaceAdapter

class DeletedmusicFragment: Fragment() {
    private var _binding: FragmentDeletedmusicBinding? = null
    private val binding: FragmentDeletedmusicBinding
        get() = requireNotNull(_binding) { "null" }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeletedmusicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val deletedmusicAdapter = DeletedmusicAdapter()
        binding.rvMusic.adapter = deletedmusicAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}