package com.kseb.smart_car.presentation.main.place

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import coil.load
import com.kseb.smart_car.R
import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceNearbyDto
import com.kseb.smart_car.databinding.FragmentPlaceDetailBinding

class PlaceDetailFragment : Fragment() {
    private var _binding: FragmentPlaceDetailBinding? = null
    private val binding: FragmentPlaceDetailBinding
        get() = requireNotNull(_binding) { "null" }

    // PlaceList 객체를 받을 변수 선언
    private var place: ResponseRecommendPlaceNearbyDto.PlaceList? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding=FragmentPlaceDetailBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // arguments에서 PlaceList 데이터를 받아옴
        place = arguments?.getParcelable("place")

        // 받은 데이터를 UI에 반영
        place?.let { place ->
            binding.ivPhoto.transitionName = "imageTransition"
            // Coil을 사용해 이미지 로드
            binding.ivPhoto.load(place.imageUrl) {
                crossfade(true) // 페이드 인 효과 적용
                placeholder(R.drawable.background_place_photo) // 이미지 로드 중 보여줄 플레이스홀더 이미지
                error(R.drawable.dororok_logo) // 에러 시 보여줄 이미지
            }
        }
    }
}