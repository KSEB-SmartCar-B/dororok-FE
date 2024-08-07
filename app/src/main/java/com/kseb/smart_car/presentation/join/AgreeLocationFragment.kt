package com.kseb.smart_car.presentation.join

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kseb.smart_car.databinding.FragmentAgreeLocationBinding

class AgreeLocationFragment: Fragment() {
    private var _binding: FragmentAgreeLocationBinding? = null
    private val binding: FragmentAgreeLocationBinding
        get() = requireNotNull(_binding) { "null" }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAgreeLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}