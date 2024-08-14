package com.kseb.smart_car.presentation.main.place

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.kseb.smart_car.R
import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceNearbyDto
import com.kseb.smart_car.databinding.FragmentPlaceRecommendBinding
import com.kseb.smart_car.presentation.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlaceRecommendFragment: Fragment() {
    private var _binding: FragmentPlaceRecommendBinding? = null
    private val binding: FragmentPlaceRecommendBinding
        get() = requireNotNull(_binding) { "null" }
    private val placeViewModel:PlaceViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
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

       /* lifecycleScope.launch {
            mainViewModel.accessToken.observe(viewLifecycleOwner){token->
                placeViewModel.getPlace(token)
                getRecommendPlace(token)
            }
        }

        val placeAdapter = PlaceAdapter(
            requireContext(),
            "nearby",
            onPlaceSave = { place ->
                // onPlaceSave 콜백에서 처리할 내용
                isSaved(token, place)
            },
            clickPlace = { place, ivPhoto ->
                // clickPlace 콜백에서 처리할 내용
                // 예를 들어, 장소를 클릭했을 때의 동작
                showDetail(place, ivPhoto)
            }
        )
        binding.rvPlace.adapter = placeAdapter*/

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