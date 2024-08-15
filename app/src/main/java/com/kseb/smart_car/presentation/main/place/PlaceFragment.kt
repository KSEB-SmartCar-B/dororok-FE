package com.kseb.smart_car.presentation.main.place

import android.Manifest
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kakao.sdk.common.KakaoSdk.appKey
import com.kakao.vectormap.LatLng
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
import com.kseb.smart_car.databinding.FragmentPlaceBinding
import com.kseb.smart_car.extension.AddFavoritePlaceState
import com.kseb.smart_car.extension.AddressState
import com.kseb.smart_car.extension.DeleteFavoritePlaceState
import com.kseb.smart_car.extension.RecommendPlaceNearbyState
import com.kseb.smart_car.presentation.main.MainViewModel
import com.kseb.smart_car.presentation.main.map.navi.LoadingDialogFragment
import com.kseb.smart_car.presentation.main.map.navi.NaviActivity
import com.kseb.smart_car.presentation.main.place.placeDetail.PlaceDetailActivity
import com.kseb.smart_car.presentation.main.place.placeDetail.PlaceDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

@AndroidEntryPoint
class PlaceFragment : Fragment() {
    private var _binding: FragmentPlaceBinding? = null
    private val binding: FragmentPlaceBinding
        get() = requireNotNull(_binding) { "null" }

    private var loadingDialog:LoadingDialogFragment?=null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val placeViewModel: PlaceViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val placeDetailViewModel:PlaceDetailViewModel by viewModels()
    private lateinit var placeAdapter: PlaceAdapter
    private lateinit var placeNearbyAdapter: PlaceNearbyAdapter

    private lateinit var knNaviView: KNNaviView
    private var latitude by Delegates.notNull<Double>()
    private var longitude by Delegates.notNull<Double>()
    private val REQUEST_CODE_LOCATION_PERMISSION = 123

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getLastKnownLocation()
        //placeAdapter= PlaceAdapter(requireContext(), "nearby")

    }

    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한이 없으면 권한 요청
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latitude=location.latitude
                    longitude=location.longitude
                    setting()
                }
            }
    }

    private fun setting() {
        val safeBinding = _binding ?: return // binding이 null이면 함수 종료

        // 필요한 Adapter 초기화
        placeAdapter = PlaceAdapter(
            onPlaceSave = { place -> isSaved(place) },
            clickPlace = { place, ivPhoto, ivCircle, btnSave -> showDetail(place, ivPhoto, ivCircle, btnSave) },
            goNavi={place->goNavi(place)}
        )

        placeNearbyAdapter = PlaceNearbyAdapter(
            onPlaceSave = { place -> isSaved(place) },
            clickPlace = { place, ivPhoto, ivCircle, btnSave -> showDetail(place, ivPhoto, ivCircle, btnSave) },
            goNavi={place->goNavi(place)}
        )

        switchRecommend()
        //저장된 장소 가져오기
        lifecycleScope.launch {
            mainViewModel.accessToken.observe(viewLifecycleOwner) { token ->
                placeViewModel.setAccessToken(token)
                placeViewModel.accessToken.observe(viewLifecycleOwner){
                    getRecommendPlaceNearby()

                    // 스크롤 리스너를 초기화 시점에 설정합니다.
                    binding.rvPlace.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)

                            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                            val visibleItemCount = layoutManager.childCount
                            val totalItemCount = layoutManager.itemCount
                            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                            if (!placeViewModel.isLoading && !placeViewModel.isLastPage) {
                                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                    && firstVisibleItemPosition >= 0
                                    && totalItemCount >= placeViewModel.pageSize!!
                                ) {
                                    // 다음 페이지를 로드합니다.
                                    placeViewModel.isLoading = true
                                    placeViewModel.getPlaceNearby(latitude, longitude, placeViewModel.pageNo)
                                }
                            }
                        }
                    })
                }
                //replaceFragment(PlaceNearbyFragment())
            }
        }
    }

    private fun getRecommendPlaceNearby() {
        lifecycleScope.launch {
            // 현재 저장된 장소 리스트를 가져옴
            val savedPlaceList = placeViewModel.savedPlaceList.value ?: emptyList()
            showShimmer(isLoading=true)

            placeViewModel.getSavedPlaceList()
            placeViewModel.getPlaceNearby(latitude, longitude)
            placeViewModel.placeNearbyState.collect { placeNearbyState ->
                when (placeNearbyState) {
                    is RecommendPlaceNearbyState.Success -> {
                        Log.d("placefragment","getrecommendplacenearby-list:${placeNearbyState.placeNearbyDto.places}")
                        binding.rvPlace.adapter = placeNearbyAdapter
                        placeNearbyAdapter.getSavedPlace(savedPlaceList)
                        placeNearbyAdapter.getNearbyPlaceList(placeNearbyState.placeNearbyDto.places)
                        showShimmer(isLoading=false)

                        if (placeViewModel.pageNo == 1) {
                            placeNearbyAdapter.getSavedPlace(savedPlaceList)
                            placeNearbyAdapter.getNearbyPlaceList(placeNearbyState.placeNearbyDto.places)
                        } else {
                            val currentPosition = (binding.rvPlace.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                            placeNearbyAdapter.getNearbyPlaceList(placeNearbyState.placeNearbyDto.places)
                            binding.rvPlace.scrollToPosition(currentPosition)
                        }
                    }

                    is RecommendPlaceNearbyState.Loading -> {}
                    is RecommendPlaceNearbyState.Error -> {
                        Log.e("placeNearbyFragment", "근처 장소 가져오기 에러!")
                    }
                }
            }
        }
    }

    private fun getRecommendPlace(){
        lifecycleScope.launch {
            // 현재 저장된 장소 리스트를 가져옴
            val savedPlaceList = placeViewModel.savedPlaceList.value ?: emptyList()
            showShimmer(isLoading=true)

            placeViewModel.getSavedPlaceList()
            placeViewModel.getPlace(latitude, longitude)
            placeViewModel.placeState.collect { placeState ->
                when (placeState) {
                    is RecommendPlaceNearbyState.Success -> {
                        binding.rvPlace.adapter = placeAdapter
                        placeAdapter.getSavedPlace(savedPlaceList)
                        placeAdapter.getNearbyPlaceList(placeState.placeNearbyDto.places)
                        showShimmer(isLoading=false)

                        if (placeViewModel.pageNo == 1) {
                            placeAdapter.getSavedPlace(savedPlaceList)
                            placeAdapter.getNearbyPlaceList(placeState.placeNearbyDto.places)
                        } else {
                            val currentPosition = (binding.rvPlace.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                            placeAdapter.getNearbyPlaceList(placeState.placeNearbyDto.places)
                            binding.rvPlace.scrollToPosition(currentPosition)
                        }

                        showShimmer(isLoading=false)
                        placeViewModel.isLoading = false

                        // 다음 페이지를 위해 페이지 번호를 증가시킵니다.
                        placeViewModel.pageNo += 1
                    }

                    is RecommendPlaceNearbyState.Loading -> {placeViewModel.isLoading = true}
                    is RecommendPlaceNearbyState.Error -> {
                        Log.e("placeNearbyFragment", "근처 장소 가져오기 에러!")
                        showShimmer(isLoading=false)
                        placeViewModel.isLoading = false
                    }
                }
            }
        }
    }

    private fun showShimmer(isLoading: Boolean) {
        if (isLoading) {
            binding.sflSample.startShimmer()
            binding.sflSample.visibility = View.VISIBLE
            binding.rvPlace.visibility = View.GONE
        } else {
            binding.sflSample.stopShimmer()
            binding.sflSample.visibility = View.GONE
            binding.rvPlace.visibility = View.VISIBLE
        }
    }

    private fun isSaved(place: ResponseRecommendPlaceNearbyDto.PlaceList) {
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
    }

    private fun showDetail(place: ResponseRecommendPlaceNearbyDto.PlaceList, sharedView: View, circle:View, saved: AppCompatButton) {
        var accessToken:String?=null
        placeViewModel.accessToken.observe(viewLifecycleOwner){token->
            accessToken=token
        }

        val intent = Intent(context, PlaceDetailActivity::class.java).apply {
            putExtra("place", place)
            putExtra("accessToken",accessToken)
        }

        val options = ActivityOptions.makeSceneTransitionAnimation(
            activity,
            android.util.Pair(sharedView, sharedView.transitionName),
            android.util.Pair(circle, circle.transitionName),
            android.util.Pair(saved, saved.transitionName)
        )

        startActivity(intent, options.toBundle())
    }


    /*private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.fcv_place, fragment)
            .commit()
    }*/

    private fun switchRecommend() {
        binding.tvNearby.setOnClickListener {
            if (binding.lineNearby.visibility == View.INVISIBLE) {
                viewText(true)
                getRecommendPlaceNearby()
            }
        }
        binding.tvRecommend.setOnClickListener{
            if (binding.lineRecommend.visibility == View.INVISIBLE) {
                viewText(false)
                getRecommendPlace()
            }
        }
    }

    private fun viewText(nearby: Boolean) {
        with(binding) {
            val nearbyColor = if (nearby) R.color.black else R.color.medium_dark
            val recommendColor = if (nearby) R.color.medium_dark else R.color.black

            tvNearby.setTextColor(ContextCompat.getColor(requireContext(), nearbyColor))
            lineNearby.visibility = if (nearby) View.VISIBLE else View.INVISIBLE

            tvRecommend.setTextColor(ContextCompat.getColor(requireContext(), recommendColor))
            lineRecommend.visibility = if (nearby) View.INVISIBLE else View.VISIBLE
        }
    }

    // Api 호출이 시작되면 LoadingDialogFragment를 보여준다.
    private fun showLoadingActivity() {
        loadingDialog = LoadingDialogFragment()
        loadingDialog?.show(parentFragmentManager, "LoadingDialog")
    }

    // 데이터 로딩이 완료 되면 LoadingDialogFragment를 dismiss 한다.
    private fun closeLoadingActivity() {
        loadingDialog?.dismiss()
    }


    private fun goNavi(place: ResponseRecommendPlaceNearbyDto.PlaceList){
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

                            showLoading(true)
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

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingLayout.visibility = View.VISIBLE
        } else {
            binding.loadingLayout.visibility = View.GONE
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
        val token = placeViewModel.accessToken.value
        if (!token.isNullOrEmpty()) {
            showLoading(false)
            placeViewModel.getSavedPlaceList()
            placeViewModel.savedPlaceList.observe(viewLifecycleOwner) { list ->
                Log.d("placeFragment","list: ${list}")
                when (binding.rvPlace.adapter) {
                    is PlaceNearbyAdapter -> {
                        (binding.rvPlace.adapter as PlaceNearbyAdapter).getSavedPlace(list)
                    }
                    is PlaceAdapter -> {
                        (binding.rvPlace.adapter as PlaceAdapter).getSavedPlace(list)
                    }
                    else -> {
                        Log.e("PlaceFragment", "Adapter is not initialized correctly")
                    }
                }
            }
        } else {
            Log.e("PlaceFragment", "AccessToken is null or empty in onResume")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}