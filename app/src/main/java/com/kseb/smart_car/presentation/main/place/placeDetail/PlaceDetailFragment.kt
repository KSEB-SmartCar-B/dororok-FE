package com.kseb.smart_car.presentation.main.place.placeDetail

import android.Manifest
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kakao.sdk.common.KakaoSdk.appKey
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakaomobility.knsdk.KNLanguageType
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.common.objects.KNError_Code_C302
import com.kakaomobility.knsdk.ui.view.KNNaviView
import com.kseb.smart_car.BuildConfig
import com.kseb.smart_car.R
import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceNearbyDto
import com.kseb.smart_car.databinding.FragmentPlaceDetailBinding
import com.kseb.smart_car.extension.AddressState
import com.kseb.smart_car.extension.GetRecommendPlaceDetailState
import com.kseb.smart_car.presentation.main.map.navi.LoadingDialogFragment
import com.kseb.smart_car.presentation.main.map.navi.NaviActivity
import com.kseb.smart_car.presentation.main.place.PlaceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class PlaceDetailFragment : Fragment() {
    private var _binding: FragmentPlaceDetailBinding? = null
    private val binding: FragmentPlaceDetailBinding
        get() = requireNotNull(_binding) { "null" }

    private var accessToken: String? = null
    private lateinit var kakaoMapView: MapView
    private val placeDetailViewModel: PlaceDetailViewModel by activityViewModels()
    private val placeViewModel:PlaceViewModel by activityViewModels()
    private lateinit var placeDetailAdapter:PlaceDetailAdapter

    private var loadingDialog:LoadingDialogFragment?=null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var knNaviView: KNNaviView
    private val REQUEST_CODE_LOCATION_PERMISSION = 123
    private var place: ResponseRecommendPlaceNearbyDto.PlaceList? = null
    private var placePosition: LatLng? = null
    val PERMISSIONS_REQUEST_CODE = 100
    var REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlaceDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰의 레이아웃이 결정된 후 너비를 설정하기 위해 ViewTreeObserver 사용
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                setting()
            }
        })
    }

    private fun setting() {
        placeDetailViewModel.accessToken.observe(viewLifecycleOwner) { token ->
            accessToken = token
        }
        // 80dp를 픽셀 단위로 변환
        val marginInPixels = (80 * resources.displayMetrics.density).toInt()
        Log.d("PlaceDetailFragment", "Margin in pixels: $marginInPixels")

        // 부모의 width를 얻어옴
        val parentWidth = (view?.parent as View).width
        Log.d("PlaceDetailFragment", "Parent width: $parentWidth")

        // MapView의 layoutParams를 가져옴
        val mapView = binding.mapView
        val layoutParams = mapView.layoutParams

        // layoutParams.width를 설정
        layoutParams.width = parentWidth - marginInPixels
        Log.d("PlaceDetailFragment", "Layout width set to: ${layoutParams.width}")

        // MapView에 변경된 layoutParams 적용
        mapView.layoutParams = layoutParams

        kakaoMapView = binding.mapView
        Log.d("PlaceDetailFragment", "MapView initialized")

        // arguments에서 place 가져오기
        arguments?.let {
            place = it.getParcelable("place")
            Log.d("PlaceDetailFragment", "Place content id: ${place?.contentId}")
        }

        // place가 있을 경우 UI 업데이트
        place?.let {
            Log.d("PlaceDetailFragment", "Place title: ${it.title}, address: ${it.address}")
            val thisPlace=it
            binding.tvPlaceName.text = thisPlace.title
            binding.tvAddress.text = thisPlace.address
            moreRecommend(thisPlace)
            binding.ivNavigation.setOnClickListener{
                clickNavi(thisPlace)
            }
        }

        // 권한 확인 및 지도 초기화
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("PlaceDetailFragment", "Location permission granted")
            setKakaoMap()
        } else {
            Log.d("PlaceDetailFragment", "Location permission denied")
            requestPermissions(REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
        }
    }


    private fun setKakaoMap() {
        kakaoMapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                // 지도 API 가 정상적으로 종료될 때 호출됨
                Log.d("placeDetailFragment", "Map destroyed")
            }

            override fun onMapError(error: Exception) {
                // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출됨
                Log.e("placeDetailFragment", "Map error: ${error.message}", error)
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                // 인증 후 API 가 정상적으로 실행될 때 호출됨
                Log.d("placeDetailFragment", "mapready")

                val initialPosition = LatLng.from(37.5665, 126.9780) // 서울시청 대략적인 위치
                kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(initialPosition))

                CoroutineScope(Dispatchers.Main).launch {
                    getMyLocation(kakaoMap)
                    if(placePosition==null){
                        Log.e("placedetailfragment","onmapready - placeposition is null")
                    }
                    if(kakaoMap==null){
                        Log.e("placedetailfragment","onmapready - kakaomap is null")
                    }
                    //kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(placePosition!!))
                }
            }

            private fun getMyLocation(kakaoMap: KakaoMap) {
                placeDetailViewModel.getAddress(place!!.title)
                lifecycleScope.launch {
                    placeDetailViewModel.addressState.collect { addressState ->
                        when (addressState) {
                            is AddressState.Success -> {
                                if (addressState.addressDto.documents.isNotEmpty()) {
                                    val lat = addressState.addressDto.documents[0].y
                                    val lng = addressState.addressDto.documents[0].x
                                    placePosition = LatLng.from(lat.toDouble(), lng.toDouble())
                                    Log.d("placeDetailFragment", "Location found: $placePosition")

                                    // 카메라 이동 및 라벨 생성
                                    withContext(Dispatchers.Main) {
                                        kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(placePosition!!))

                                        // label 생성
                                        val styles = kakaoMap.labelManager!!
                                            .addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.pin)))
                                        val options = LabelOptions.from(placePosition).setStyles(styles)
                                        val layer = kakaoMap.labelManager!!.layer
                                        layer?.addLabel(options)
                                    }
                                } else {
                                    Log.e("placeDetailFragment", "Address documents are empty.")
                                }
                            }
                            is AddressState.Loading -> {
                                Log.d("placeDetailFragment", "Loading address...")
                            }
                            is AddressState.Error -> {
                                Log.e("placeDetailFragment", "Error loading address: ${addressState.message}")
                            }
                        }
                    }
                }
            }

            override fun getPosition(): LatLng {
                // 위치 정보가 설정된 후 UI 업데이트를 메인 스레드에서 실행
                Log.d("placeDetailFragment","position: ${placePosition}")
                return placePosition ?: LatLng.from(37.406960, 127.115587)
            }

            override fun getZoomLevel(): Int {
                // 지도 시작 시 확대/축소 줌 레벨 설정
                return 15
            }

            /*override fun getMapViewInfo(): MapViewInfo {
                // 지도 시작 시 App 및 MapType 설정
                return MapViewInfo.from(MapType.NORMAL.value)
            }*/

            override fun getViewName(): String {
                // KakaoMap 의 고유한 이름을 설정
                return "DororokMap"
            }

            override fun isVisible(): Boolean {
                // 지도 시작 시 visible 여부를 설정
                return true
            }

            override fun getTag(): String {
                // KakaoMap 의 tag 을 설정
                return "DororokMapTag"
            }
        })
    }

    private fun moreRecommend(place: ResponseRecommendPlaceNearbyDto.PlaceList) {
        // `~~시` 패턴만 추출하여 `tvWhere`에 저장
        Log.d("PlaceDetailFragment", "more recommend - Place title: ${place.title}")
        val cityPattern = Regex("""(\S+시)""")
        val matchResult = cityPattern.find(place.address)
        matchResult?.let { match ->
            val cityName = match.value
            binding.tvWhere.text = cityName
            getMoreList(place)
            Log.d("PlaceDetailFragment", "Extracted city name: $cityName")

        } ?: run {
            // `~~시`가 없는 경우 처리
            Log.d("PlaceDetailFragment", "No city name found in title")
            binding.tvWhere.text = "Unknown" // 또는 다른 기본값 설정
        }
    }

    private fun getMoreList(place: ResponseRecommendPlaceNearbyDto.PlaceList) {
        placeDetailViewModel.accessToken.observe(viewLifecycleOwner){token->
            placeDetailAdapter = PlaceDetailAdapter(
                onPlaceSave = { place -> /*clickSave(place)*/ },
                clickPlace = { place, ivPhoto, /*ivCircle, btnSave*/ -> showDetail(place, ivPhoto, /*ivCircle, btnSave*/) }
            )
            binding.rvDetail.adapter=placeDetailAdapter

            Log.d("placedetailfragment","contentid:${place.contentId} token:${token}")
            placeDetailViewModel.getDetail(token, place.contentId)
            lifecycleScope.launch {
                placeDetailViewModel.detailState.collect{detailState->
                    when(detailState){
                        is GetRecommendPlaceDetailState.Success -> {
                            placeDetailAdapter.getNearbyPlaceList(detailState.detailDto.placeList)
                            Log.d("placedtailfargment","getmorelist nearbylist:${detailState.detailDto.placeList}")
                        }
                        is GetRecommendPlaceDetailState.Loading->{}
                        is GetRecommendPlaceDetailState.Error->{
                            Log.d("placedetailfragment","getmorelist error: ${detailState.message}")
                        }
                    }
                }
            }
        }
    }/*
    private fun clickSave(place: ResponseRecommendPlaceNearbyDto.PlaceList) {
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
                        }

                        is AddFavoritePlaceState.Loading -> {}
                        is AddFavoritePlaceState.Error -> {
                            Log.e("placeNearbyFragment", "저장된 장소 저장 에러: ${saveState.message}")
                        }
                    }
                }
            }
        }
    }*/

    private fun showDetail(place: ResponseRecommendPlaceNearbyDto.PlaceList, sharedView: View,/* circle:View, saved: AppCompatButton*/) {
        val intent = Intent(context, PlaceDetailActivity::class.java).apply {
            putExtra("place", place)
            Log.d("placedetailfragment","accessToken:${accessToken}")
            putExtra("accessToken",accessToken)
        }

        val options = ActivityOptions.makeSceneTransitionAnimation(
            activity,
            android.util.Pair(sharedView, sharedView.transitionName),
           /* android.util.Pair(circle, circle.transitionName),
            android.util.Pair(saved, saved.transitionName)*/
        )

        startActivity(intent, options.toBundle())
    }

    // Api 호출이 시작되면 LoadingDialogFragment를 보여준다.
    private fun showLoadingActivity() {
        loadingDialog = LoadingDialogFragment("navi")
        loadingDialog?.show(parentFragmentManager, "LoadingDialog")
    }

    // 데이터 로딩이 완료 되면 LoadingDialogFragment를 dismiss 한다.
    private fun closeLoadingActivity() {
        loadingDialog?.dismiss()
    }

    private fun clickNavi(place: ResponseRecommendPlaceNearbyDto.PlaceList){
        placeDetailViewModel.getAddress(place!!.title)
        lifecycleScope.launch {
            placeDetailViewModel.addressState.collect { addressState ->
                when (addressState) {
                    is AddressState.Success -> {
                        if (addressState.addressDto.documents.isNotEmpty()) {
                            val lat = addressState.addressDto.documents[0].y
                            val lng = addressState.addressDto.documents[0].x
                            val placePosition = LatLng.from(lat.toDouble(), lng.toDouble())
                            Log.d("placeDetailFragment", "Location found: $placePosition")

                            showLoadingActivity()
                            setKakaoNavi(lat.toDouble(), lng.toDouble(), place.title)
                        } else {
                            Log.e("placeDetailFragment", "Address documents are empty.")
                        }
                    }
                    is AddressState.Loading -> {
                        Log.d("placeDetailFragment", "Loading address...")
                    }
                    is AddressState.Error -> {
                        Log.e("placeDetailFragment", "Error loading address: ${addressState.message}")
                    }
                }
            }
        }
    }

    private fun setKakaoNavi(x: Double, y: Double, placeName: String) {
        knNaviView = KNNaviView(requireActivity())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        checkLocation(x,y, placeName)
    }

    private fun initializeKNSDK(
        x: Double,
        y: Double,
        placeName: String,
        currentLatitude: Double,
        currentLongitude: Double
    ) {
        KNSDK.initializeWithAppKey(
            appKey, BuildConfig.VERSION_NAME,
            null, KNLanguageType.KNLanguageType_KOREAN, aCompletion = {
                if (it != null) {
                    when (it.code) {
                        KNError_Code_C302 -> {
                            Log.e("error", "error code c302")
                        }

                        else -> {
                            Log.e("error", "unknown error")
                        }
                    }
                } else {
                    // 인증 완료
                    Log.d("searchFragment", "인증완료")
                    Log.d("searchFragment", "initialize -> 현재좌표: ${currentLongitude}, ${currentLatitude}")
                    startNavi(x,y, placeName, currentLatitude, currentLongitude)
                }
            })
    }

    private fun checkLocation(x: Double, y: Double, placeName: String) {
        // 위치 권한이 이미 허용되어 있는지 확인
        if (isLocationPermissionGranted()) {
            // 위치 기능 사용 가능
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 권한이 없는 경우 처리
                requestLocationPermission(x,y,placeName)
                return
            }

            // 위치 요청
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    // 위치 정보를 가져온 후 처리
                    val currentLongitude = location.longitude
                    val currentLatitude = location.latitude
                    Log.d("goal", "long:${x}, lati:${y}")
                    initializeKNSDK(x,y, placeName, currentLatitude, currentLongitude)
                } else {
                    Log.e("searchFragment", "Location is null")
                }
            }
        } else {
            // 위치 권한 요청
            requestLocationPermission(x,y,placeName)
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission(x: Double,y: Double, placeName: String) {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_CODE_LOCATION_PERMISSION
        )
        checkLocation(x, y, placeName)
    }

    private fun startNavi(x: Double, y: Double, placeName: String, currentLatitude: Double, currentLongitude: Double){
        Log.d("placefragment","current:${currentLatitude}, ${currentLongitude} / goal:${x}, ${y}")

        val intent=Intent(requireActivity(), NaviActivity::class.java)
        intent.putExtra("currentLongitude",currentLongitude)
        intent.putExtra("currentLatitude",currentLatitude)
        intent.putExtra("goalLongitude",y)
        intent.putExtra("goalLatitude",x)
        intent.putExtra("placeName",placeName)
        Log.d("searchFragment","longitude: ${x}, latitude: ${y}")
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        val safeBinding = _binding ?: return // binding이 null이면 함수 종료
        // accessToken이 null인지 확인하고, null이 아니면 getSavedPlaceList 호출
        closeLoadingActivity()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}