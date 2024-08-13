package com.kseb.smart_car.presentation.main.place

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.FragmentPlaceBinding
import com.kseb.smart_car.presentation.main.MainViewModel
import kotlinx.coroutines.launch

class PlaceFragment: Fragment() {
    private var _binding: FragmentPlaceBinding? = null
    private val binding: FragmentPlaceBinding
        get() = requireNotNull(_binding) { "null" }
    private val placeViewModel:PlaceViewModel by activityViewModels()
    private val mainViewModel:MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //저장된 장소 가져오기
        lifecycleScope.launch {
            mainViewModel.accessToken.observe(viewLifecycleOwner){token->
                placeViewModel.getSavedPlaceList(token)
                replaceFragment(PlaceNearbyFragment())
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.fcv_place, fragment)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}