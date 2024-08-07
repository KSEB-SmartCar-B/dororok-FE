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
import coil.transform.CircleCropTransformation
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
    }

    private fun setting(){
        mainViewModel.accessToken.observe(viewLifecycleOwner){token ->
            myViewModel.setAccessToken(token)
            myViewModel.accessToken.observe(viewLifecycleOwner){token ->
                myViewModel.getInfo(token)
                clickButtonGenre(token)
                clickButtonInfo(token)
                clickButtonMusic(token)
                clickButtonPlace(token)
            }
            Log.d("myfragment","token:${token}")
        }

        lifecycleScope.launch {
            myViewModel.infoState.collect{infoState ->
                when(infoState){
                    is InfoState.Success -> {
                        binding.tvWelcome.text=getString(R.string.my_welcome, infoState.infoDto.nickname)
                        binding.ivProfile.load(infoState.infoDto.profile) {
                            crossfade(true)
                            placeholder(R.drawable.ic_basicprofile_foreground)
                            transformations(CircleCropTransformation()) // 이미지 원형으로
                            size(200, 200) // 이미지 크기 조정(이라는데 화질이 바뀌는 것 같음. 고고익선)
                        }
                        Log.d("myfragment","nickname: ${infoState.infoDto.nickname}")
                        myViewModel.setInfoStateLoading()
                    }
                    is InfoState.Loading ->{}
                    is InfoState.Error -> {
                        Log.e("myFragment","정보 가져오기 실패..: ${infoState.message} ")
                    }
                }
            }
        }
    }

    private fun clickButtonInfo(accessToken:String) {
        binding.info.setOnClickListener {
            Log.d("myFragment","accesstoken: $accessToken")
                startActivity(Intent(requireContext(), InformationActivity::class.java).putExtra("info",myViewModel.getMyInfo()).putExtra("accessToken",accessToken))
        }
    }

    private fun clickButtonGenre(accessToken:String) {
        binding.genre.setOnClickListener {
            startActivity(Intent(requireContext(), GenreActivity::class.java).putExtra("token", accessToken))
        }
    }

    private fun clickButtonMusic(accessToken:String) {
        binding.music.setOnClickListener {
            startActivity(Intent(requireContext(), MusicActivity::class.java).putExtra("token", accessToken))
        }
    }

    private fun clickButtonPlace(accessToken:String) {
        binding.place.setOnClickListener {
            startActivity(Intent(requireContext(), PlaceActivity::class.java).putExtra("token", accessToken))
        }
    }

    // 이 메서드를 호출하여 데이터를 새로고침할 수 있습니다.
    fun refreshData() {
        setting()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}