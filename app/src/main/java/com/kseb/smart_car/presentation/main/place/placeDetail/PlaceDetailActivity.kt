package com.kseb.smart_car.presentation.main.place.placeDetail

import android.os.Bundle
import android.transition.Transition
import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.kseb.smart_car.R
import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceNearbyDto
import com.kseb.smart_car.databinding.ActivityPlaceDetailBinding
import com.kseb.smart_car.extension.AddFavoritePlaceState
import com.kseb.smart_car.extension.DeleteFavoritePlaceState
import com.kseb.smart_car.extension.ExistFavoritePlaceState
import com.kseb.smart_car.presentation.main.place.PlaceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlaceDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaceDetailBinding
    private val placeDetailViewModel: PlaceDetailViewModel by viewModels()
    private val placeViewModel:PlaceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBind()
        setting()
    }

    private fun initBind() {
        binding = ActivityPlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setting() {
        val place = intent.getParcelableExtra<ResponseRecommendPlaceNearbyDto.PlaceList>("place")
        val imageTransitionName = intent.getStringExtra("image_transition_name")
        // 트랜지션 네임 설정
        binding.ivPhoto.transitionName = imageTransitionName

        if(intent.getStringExtra("circle_transition_name")!=null){
            val circleTransitionName=intent.getStringExtra("circle_transition_name")
            val savedTransitionName=intent.getStringExtra("saved_transition_name")
            binding.ivCircle.transitionName = circleTransitionName
            binding.btnSave.transitionName = savedTransitionName
        }

        // 트랜지션 애니메이션 적용
        postponeEnterTransition()
        loadImageWithCoil(binding.ivPhoto, place)
        isSaved(place)
        startPostponedEnterTransition()

        // 전환 애니메이션이 끝난 후에 버튼 활성화 및 클릭 리스너 설정
        binding.ivCircle.setOnClickListener(null) // 초기화 중에는 클릭 비활성화
        window.sharedElementEnterTransition.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {}

            override fun onTransitionEnd(transition: Transition) {
                // 전환 애니메이션이 끝나면 버튼 클릭 가능하도록 설정
                // 전환 애니메이션이 끝난 후에 레이아웃을 조정
                val iv = binding.ivPhoto
                val fcvPlaceDetail = binding.fcvPlaceDetail

                val photoHeight = iv.height
                val marginTop = photoHeight - TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    40f,
                    resources.displayMetrics
                ).toInt()

                val layoutParams = fcvPlaceDetail.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.topMargin = marginTop
                fcvPlaceDetail.layoutParams = layoutParams

                // 레이아웃이 변경된 후 다시 그려지도록 요청
                fcvPlaceDetail.requestLayout()
               clickButton(place!!)
            }

            override fun onTransitionCancel(transition: Transition) {}

            override fun onTransitionPause(transition: Transition) {}

            override fun onTransitionResume(transition: Transition) {}
        })
    }

    private fun loadImageWithCoil(
        imageView: ImageView,
        place: ResponseRecommendPlaceNearbyDto.PlaceList?
    ) {
        // 시스템 상단바 높이를 가져옴
        imageView.setOnApplyWindowInsetsListener { view, insets ->
            val statusBarHeight = insets.systemWindowInsetTop
            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = statusBarHeight // 시스템 상단바 높이만큼 마진 설정
            view.layoutParams = layoutParams

            // insets을 그대로 반환하여 다른 뷰들도 이 값을 사용할 수 있도록 함
            insets
        }

        // 인셋을 적용하기 위해서 인셋을 강제로 요청
        imageView.requestApplyInsets()

        // Coil을 사용하여 이미지 로드
        imageView.load(place!!.imageUrl) { // 실제 이미지 URL 또는 리소스
            listener(
                onSuccess = { _, _ ->
                    // 이미지가 로드된 후 전환 애니메이션 시작
                    startPostponedEnterTransition()
                },
                onError = { _, _ ->
                    // 에러 발생 시에도 전환 애니메이션을 시작 (필요 시)
                    //startPostponedEnterTransition()
                }
            )
        }
    }

    private fun isSaved(place: ResponseRecommendPlaceNearbyDto.PlaceList?) {
        val accessToken = intent.getStringExtra("accessToken")
        placeDetailViewModel.setAccessToken(accessToken!!)
        Log.d("placeDetailActivity","accessToken:${accessToken}, contentId: ${place!!.contentId}")
        if (accessToken != null) {
            placeDetailViewModel.existFavoritePlace(accessToken, place!!.contentId)
            lifecycleScope.launch {
                placeDetailViewModel.existState.collect { existState ->
                    when (existState) {
                        is ExistFavoritePlaceState.Success -> {
                            binding.btnSave.isSelected = existState.response
                            replaceFragment(PlaceDetailFragment(), place)
                        }
                        is ExistFavoritePlaceState.Loading -> {}
                        is ExistFavoritePlaceState.Error -> {
                            Log.e("placeDetailActivity", "저장된 장소 저장 에러: ${existState.message}")
                        }

                    }
                }
            }
        }
    }

    private fun clickButton(place: ResponseRecommendPlaceNearbyDto.PlaceList) {
        val accessToken = intent.getStringExtra("accessToken")

        binding.btnSave.setOnClickListener{
            placeViewModel.setAccessToken(accessToken!!)
            placeViewModel.accessToken.observe(this@PlaceDetailActivity){token->
                savePlace(place)
            }
        }

        binding.btnBack.setOnClickListener {
            supportFinishAfterTransition()
        }
    }

    private fun savePlace(place: ResponseRecommendPlaceNearbyDto.PlaceList) {
        Log.d("placeNearbyFragment", "place clicked!:${place.title}")

        // 현재 저장된 장소 리스트를 가져옴
        val savedPlaceList = placeViewModel.savedPlaceList.value ?: emptyList()

        var exist = false
        for (savedPlace in savedPlaceList) {
            if (place.contentId == savedPlace.contentId) {
                placeViewModel.deleteFavoritePlace(savedPlace.contentId)
                lifecycleScope.launch {
                    placeViewModel.deleteNearbyState.collect { deleteState ->
                        when (deleteState) {
                            is DeleteFavoritePlaceState.Success -> {
                                Log.d("placeNearbyFragment", "저장된 장소 삭제 성공!")
                                placeViewModel.getSavedPlaceList()
                                placeViewModel.setDeleteLoading()
                                binding.btnSave.isSelected=false
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
            placeViewModel.addFavoritePlace(
                place.title,
                place.address,
                place.imageUrl,
                place.contentId
            )
            lifecycleScope.launch {
                placeViewModel.addNearbyState.collect { saveState ->
                    when (saveState) {
                        is AddFavoritePlaceState.Success -> {
                            Log.d("placeNearbyFragment", "장소 저장 성공!")
                            placeViewModel.getSavedPlaceList()
                            placeViewModel.setSaveLoading()
                            binding.btnSave.isSelected=true
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

    private fun replaceFragment(
        fragment: Fragment,
        place: ResponseRecommendPlaceNearbyDto.PlaceList?
    ) {
        place?.let {
            val bundle = Bundle().apply {
                putParcelable("place", it)  // place 객체를 Bundle에 저장
            }
            fragment.arguments = bundle  // Fragment에 Bundle을 설정
        }

        Log.d("placedtailactivity","fragment start")
        supportFragmentManager.beginTransaction()
            .replace(R.id.fcv_place_detail, fragment)
            .commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        supportFinishAfterTransition()  // 트랜지션 애니메이션과 함께 종료
    }

}