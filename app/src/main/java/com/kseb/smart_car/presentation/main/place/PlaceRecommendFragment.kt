package com.kseb.smart_car.presentation.main.place

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.kseb.smart_car.R
import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceNearbyDto
import com.kseb.smart_car.databinding.FragmentPlaceRecommendBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaceRecommendFragment: Fragment() {
    private var _binding: FragmentPlaceRecommendBinding? = null
    private val binding: FragmentPlaceRecommendBinding
        get() = requireNotNull(_binding) { "null" }
    private val placeViewModel:PlaceViewModel by activityViewModels()
    private lateinit var placeAdapter: PlaceAdapter

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

        placeAdapter = PlaceAdapter(requireContext(),"place") { place -> isSaved(place) }
        binding.rvPlace.adapter = placeAdapter

        clickButtonNearby()
    }

    private fun isSaved(place: ResponseRecommendPlaceNearbyDto.PlaceList){

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