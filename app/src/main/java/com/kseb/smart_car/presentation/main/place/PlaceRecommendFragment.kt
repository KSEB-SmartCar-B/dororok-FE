package com.kseb.smart_car.presentation.main.place

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.FragmentPlaceRecommendBinding

class PlaceRecommendFragment: Fragment() {
    private var _binding: FragmentPlaceRecommendBinding? = null
    private val binding: FragmentPlaceRecommendBinding
        get() = requireNotNull(_binding) { "null" }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceRecommendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val placeAdapter = PlaceAdapter()
        binding.rvPlace.adapter = placeAdapter

        clickButtonNearby()
    }

    private fun clickButtonNearby() {
        binding.tvNearby.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fcv_place, PlaceNearbyFragment())
            transaction.commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}