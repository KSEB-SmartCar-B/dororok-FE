package com.kseb.smart_car.presentation.main.my

import android.content.Intent
import android.icu.text.IDNA.Info
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.FragmentMyBinding
import com.kseb.smart_car.extension.GenreState
import com.kseb.smart_car.extension.InfoState
import com.kseb.smart_car.presentation.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyFragment: Fragment() {
    private var _binding: FragmentMyBinding?= null
    private val binding: FragmentMyBinding
        get() = requireNotNull(_binding) {"null"}
    private val mainViewModel:MainViewModel by activityViewModels()
    private val myViewModel:MyViewModel by viewModels()

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

        setting()
        clickButtonInformation()
        clickButtonGenre()
    }

    private fun setting(){
        mainViewModel.accessToken.observe(viewLifecycleOwner){token ->
            myViewModel.token = token
            myViewModel.getInfo(token)
            Log.d("myfragment","token:${token}")
        }

        lifecycleScope.launch {
            myViewModel.infoState.collect{infoState ->
                when(infoState){
                    is InfoState.Success -> {
                        binding.tvWelcome.text=getString(R.string.my_welcome, infoState.infoDto.nickname)
                        binding.ivProfile.load(infoState.infoDto.profile)
                        Log.d("myfragment","nickname: ${infoState.infoDto.nickname}")
                    }
                    is InfoState.Loading ->{}
                    is InfoState.Error -> {
                        Log.e("myFragment","정보 가져오기 실패..: ${infoState.message} ")
                    }
                }
            }
        }
    }

    private fun clickButtonInformation() {
        binding.ibInformation.setOnClickListener {
            if(myViewModel.getMyInfo()!=null){
                startActivity(Intent(requireContext(), InformationActivity::class.java).putExtra("info", myViewModel.getMyInfo()))
            }
        }
    }

    private fun clickButtonGenre() {
        binding.ibGenre.setOnClickListener {
            startActivity(Intent(requireContext(), GenreActivity::class.java).putExtra("token", myViewModel.token))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}