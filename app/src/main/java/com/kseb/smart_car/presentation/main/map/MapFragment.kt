package com.kseb.smart_car.presentation.main.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapGravity
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapType
import com.kakao.vectormap.MapView
import com.kakao.vectormap.MapViewInfo
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.FragmentMapBinding
import com.kseb.smart_car.presentation.main.MainViewModel
import com.kseb.smart_car.presentation.main.map.navi.search.SearchActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapFragment : Fragment() {
    private var _binding: FragmentMapBinding? = null
    private val binding: FragmentMapBinding
        get() = requireNotNull(_binding) { "null" }
    private lateinit var kakaoMapView: MapView
    private val mainViewModel: MainViewModel by activityViewModels()

    private var currentPosition: LatLng? = null
    val PERMISSIONS_REQUEST_CODE = 100
    var REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setting()
    }

    private fun setting() {
        //맵 초기화
        kakaoMapView = MapView(requireContext())
        binding.mapView.addView(kakaoMapView)
        // 권한 확인 및 지도 초기화
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setKakaoMap()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
        }
        clickButtonSearch()
    }
    private fun setKakaoMap() {
        kakaoMapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                // 지도 API 가 정상적으로 종료될 때 호출됨
                Log.d("mapFragment", "Map destroyed")
            }

            override fun onMapError(error: Exception) {
                // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출됨
                Log.e("mapFragment", "Map error: ${error.message}", error)
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                // 인증 후 API 가 정상적으로 실행될 때 호출됨
                Log.d("mapFragment", "mapready")

                // 지도 로드 성공 시 동작
                try {
                    //방위 컴패스
                    val compass = kakaoMap.compass
                    compass!!.setPosition(MapGravity.TOP or MapGravity.LEFT, 80f, 300f)
                    compass.setBackToNorthOnClick(true)
                    compass.show()

                    CoroutineScope(Dispatchers.IO).launch {
                        getMyLocation(requireContext(), kakaoMap)
                        getPosition()
                        kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(currentPosition!!))
                    }

                    binding.btnCurrentLocation.setOnClickListener {
                        kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(currentPosition!!))
                    }
                } catch (e: Exception) {
                    Log.e("mapFragment", "Error during map setup: ${e.message}", e)
                }
            }

            private fun getMyLocation(context: Context, kakaoMap: KakaoMap) {
                val permissionCheck = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    Log.d("mainactivity", "permission_granted")
                    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    try {
                        val userCurLocation =
                            lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        /*val uLatitude = userCurLocation!!.latitude
                        val uLogitude = userCurLocation.longitude*/
                        //Log.d("mainactivity", "latitude: ${uLatitude}, logitude: ${uLogitude}")
                        currentPosition = LatLng.from(37.290184417162514, 126.57855020444013)

                        //label 생성
                        val styles = kakaoMap.labelManager!!
                            .addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.pin)))
                        val options =
                            LabelOptions.from(LatLng.from(currentPosition)).setStyles(styles)
                        val layer = kakaoMap.labelManager!!.layer
                        val label = layer!!.addLabel(options)


                        //trackingManager 설정
                        /*val trackingManager = kakaoMap.trackingManager
                        trackingManager!!.startTracking(label)*/

                    } catch (e: java.lang.NullPointerException) {
                        Log.e("LOCATION_ERROR", e.toString())
                    }
                } else {
                    Log.d("mainactivity", "Permission denied")
                    // 위치 정보 권한 요청
                    requestPermissions(
                        REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE
                    )
                }
            }

            override fun getPosition(): LatLng {
                // 지도 시작 시 위치 좌표를 설정
                Log.d("mainactivity", "현재위치: ${currentPosition}")
                return currentPosition ?: LatLng.from(37.406960, 127.115587)
            }

            override fun getZoomLevel(): Int {
                // 지도 시작 시 확대/축소 줌 레벨 설정
                return 16
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

    private fun clickButtonSearch() {
        mainViewModel.accessToken.observe(viewLifecycleOwner) { token ->
            binding.svSearch.setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    val intent = Intent(requireContext(), SearchActivity::class.java)
                    intent.putExtra("accessToken", token)
                    startActivity(intent)
                    //맵 화면에서 키패드 안보이게 함
                    binding.svSearch.clearFocus()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}