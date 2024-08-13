package com.kseb.smart_car.presentation.main.my.place

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.FragmentSavedplaceBinding
import com.kseb.smart_car.presentation.main.place.PlaceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SavedPlaceFragment : Fragment() {
    private var _binding: FragmentSavedplaceBinding? = null
    private val binding: FragmentSavedplaceBinding
        get() = requireNotNull(_binding) { "null" }

    private val placeViewModel: PlaceViewModel by viewModels()
    private val savedPlaceViewModel: SavedPlaceViewModel by activityViewModels()
    private lateinit var savedPlaceAdapter: SavedPlaceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedplaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setting()
        clickEditButton()
    }

    private fun setting() {
        savedPlaceAdapter = SavedPlaceAdapter()
        binding.rvPlace.adapter = savedPlaceAdapter

        // 데이터를 관찰하는 observer 설정
        placeViewModel.savedPlaceList.observe(viewLifecycleOwner) { list ->
            if (list != null) {
                savedPlaceAdapter.setList(list)
                Log.d("savedPlaceFragment", "list: $list")
            } else {
                Log.d("savedPlaceFragment", "list is null")
            }
        }

        // accessToken 관찰하여 저장된 장소 리스트 요청
        savedPlaceViewModel.accessToken.observe(viewLifecycleOwner) { token ->
            if (token != null) {
                Log.d("SavedplaceFragment", "accessToken observed: $token")
                placeViewModel.getSavedPlaceList(token)
            } else {
                Log.d("SavedplaceFragment", "accessToken is null")
            }
        }
    }

    private fun clickEditButton() {
        binding.btnEdit.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fcv_place, DeletedPlaceFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}