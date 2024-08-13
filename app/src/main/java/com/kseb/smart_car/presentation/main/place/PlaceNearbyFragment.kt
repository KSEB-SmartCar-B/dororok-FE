package com.kseb.smart_car.presentation.main.place

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kseb.smart_car.R
import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceNearbyDto
import com.kseb.smart_car.databinding.FragmentPlaceNearbyBinding
import com.kseb.smart_car.extension.DeleteFavoritePlaceState
import com.kseb.smart_car.extension.RecommendPlaceNearbyState
import com.kseb.smart_car.extension.AddFavoritePlaceState
import com.kseb.smart_car.presentation.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlaceNearbyFragment: Fragment() {
    private var _binding: FragmentPlaceNearbyBinding? = null
    private val binding: FragmentPlaceNearbyBinding
        get() = requireNotNull(_binding) { "null" }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val mainViewModel: MainViewModel by activityViewModels()
    private val placeViewModel:PlaceViewModel by activityViewModels()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceNearbyBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getLastKnownLocation()
    }

    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 권한 요청
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    setting(location.latitude, location.longitude)
                }
            }
    }

    private fun setting(latitude: Double, longitude: Double) {
        lifecycleScope.launch {
            mainViewModel.accessToken.observe(viewLifecycleOwner){token->
                Log.d("placeNearbyFragment","token:${token}, latitude: ${latitude}, longitude:${longitude}")
                placeViewModel.getPlaceNearby(token,latitude, longitude)
                getRecommendPlaceNearby(token)
            }
        }
        clickButtonRecommend()
    }

    private fun getRecommendPlaceNearby(token: String) {
        lifecycleScope.launch {
            // 현재 저장된 장소 리스트를 가져옴
            val savedPlaceList = placeViewModel.savedPlaceList.value ?: emptyList()

            placeViewModel.placeNearbyState.collect{placeNearbyState ->
                when(placeNearbyState){
                    is RecommendPlaceNearbyState.Success -> {
                        val placeAdapter = PlaceAdapter(requireContext(),"nearby") { place ->
                            isSaved(
                                token, place
                            )
                        }
                        binding.rvPlace.adapter = placeAdapter
                        placeAdapter.getSavedPlace(savedPlaceList)
                        placeAdapter.getNearbyPlaceList(placeNearbyState.placeNearbyDto.places)
                    }
                    is RecommendPlaceNearbyState.Loading->{}
                    is RecommendPlaceNearbyState.Error->{
                        Log.e("placeNearbyFragment", "근처 장소 가져오기 에러!")
                    }
                }
            }
        }
    }

    private fun isSaved(token: String, place: ResponseRecommendPlaceNearbyDto.PlaceList) {
        Log.d("placeNearbyFragment", "place clicked!:${place.title}")

        // 현재 저장된 장소 리스트를 가져옴
        val savedPlaceList = placeViewModel.savedPlaceList.value ?: emptyList()

        var exist = false
        for (savedPlace in savedPlaceList) {
            if (place.contentId == savedPlace.contentId) {
                placeViewModel.deleteFavoritePlace(token, savedPlace.contentId)
                lifecycleScope.launch {
                    placeViewModel.deleteNearbyState.collect { deleteState ->
                        when (deleteState) {
                            is DeleteFavoritePlaceState.Success -> {
                                Log.d("placeNearbyFragment", "저장된 장소 삭제 성공!")
                                placeViewModel.getSavedPlaceList(token)
                                placeViewModel.setDeleteLoading()
                            }
                            is DeleteFavoritePlaceState.Loading -> {}
                            is DeleteFavoritePlaceState.Error -> {
                                Log.e("placeNearbyFragment", "저장된 장소 삭제 에러: ${deleteState.message}")
                            }
                        }
                    }
                }
                exist = true
                break
            }
        }
        if (!exist) {
            placeViewModel.addFavoritePlace(token, place.title, place.address, place.imageUrl, place.contentId)
            lifecycleScope.launch {
                placeViewModel.addNearbyState.collect { saveState ->
                    when (saveState) {
                        is AddFavoritePlaceState.Success -> {
                            Log.d("placeNearbyFragment", "장소 저장 성공!")
                            placeViewModel.getSavedPlaceList(token)
                            placeViewModel.setSaveLoading()
                        }
                        is AddFavoritePlaceState.Loading -> {}
                        is AddFavoritePlaceState.Error -> {
                            Log.e("placeNearbyFragment", "저장된 장소 저장 에러: ${saveState.message}")
                        }
                    }
                }
            }
        }
    }


    private fun clickButtonRecommend() {
        binding.tvRecommend.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fcv_place, PlaceRecommendFragment())
            transaction.commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}