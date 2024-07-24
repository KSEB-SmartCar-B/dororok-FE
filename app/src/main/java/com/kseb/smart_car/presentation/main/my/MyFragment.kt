package com.kseb.smart_car.presentation.main.my

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kseb.smart_car.databinding.FragmentMyBinding

class MyFragment: Fragment() {
    private var _binding: FragmentMyBinding?= null
    private val binding: FragmentMyBinding
        get() = requireNotNull(_binding) {"null"}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clickButtonInformation()
        clickButtonGenre()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun clickButtonInformation() {
        binding.ibInformation.setOnClickListener {
            startActivity(Intent(requireContext(), InformationActivity::class.java))
        }
    }

    private fun clickButtonGenre() {
        binding.ibGenre.setOnClickListener {
            startActivity(Intent(requireContext(), GenreActivity::class.java))
        }
    }
}