package com.kseb.smart_car.presentation.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapGravity
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdate
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentPosition:LatLng?=null
    val PERMISSIONS_REQUEST_CODE = 100
    var REQUIRED_PERMISSIONS = arrayOf<String>(
        Manifest.permission.ACCESS_FINE_LOCATION)
    private var isFirstLoad=true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBinds()
        setting()
    }

    private fun initBinds() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setting() {
        val mapView = MapView(this)
        binding.mapView.addView(mapView)

        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                // 지도 API 가 정상적으로 종료될 때 호출됨
            }

            override fun onMapError(error: Exception) {
                // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출됨
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                // 인증 후 API 가 정상적으로 실행될 때 호출됨
                Log.d("mainactivity","mapready")
                val compass=kakaoMap.compass
                compass!!.setPosition(MapGravity.TOP or MapGravity.LEFT, 50f, 40f)
                compass.setBackToNorthOnClick(true)
                compass.show()

                CoroutineScope(Dispatchers.IO).launch {
                    getMyLocation(this@MainActivity, kakaoMap)
                    getPosition()
                    kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(currentPosition!!))
                }

            }

            private fun getMyLocation(context: Context, kakaoMap: KakaoMap){
                val permissionCheck = ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    Log.d("mainactivity","permission_granted")
                    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    try {
                        val userCurLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        val uLatitude = userCurLocation!!.latitude
                        val uLogitude = userCurLocation.longitude
                        Log.d("mainactivity","latitude: ${uLatitude}, logitude: ${uLogitude}")
                        currentPosition=LatLng.from(uLatitude,uLogitude)

                        //label 생성
                        val styles=kakaoMap.labelManager!!
                            .addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.pin)))
                        val options=LabelOptions.from(LatLng.from(currentPosition)).setStyles(styles)
                        val layer= kakaoMap.labelManager!!.layer
                        val label = layer!!.addLabel(options)


                        //trackingManager 설정
                        /*val trackingManager = kakaoMap.trackingManager
                        trackingManager!!.startTracking(label)*/
                    } catch (e: java.lang.NullPointerException) {
                        Log.e("LOCATION_ERROR", e.toString())
                    }
                }else {
                    Log.d("mainactivity", "Permission denied")
                    // 위치 정보 권한 요청
                    ActivityCompat.requestPermissions(context as Activity, REQUIRED_PERMISSIONS,PERMISSIONS_REQUEST_CODE)
                }
            }

            override fun getPosition(): LatLng {
                // 지도 시작 시 위치 좌표를 설정
                Log.d("mainactivity","현재위치: ${currentPosition}")
                return currentPosition?: LatLng.from(37.406960, 127.115587)
            }

            override fun getZoomLevel(): Int {
                // 지도 시작 시 확대/축소 줌 레벨 설정
                return 15
            }

           /* override fun getMapViewInfo(): MapViewInfo {
                // 지도 시작 시 App 및 MapType 설정
                return MapViewInfo.from(MapType.NORMAL.name)
            }*/

            override fun getViewName(): String {
                // KakaoMap 의 고유한 이름을 설정
                return "MyFirstMap"
            }

            override fun isVisible(): Boolean {
                // 지도 시작 시 visible 여부를 설정
                return true
            }

            override fun getTag(): String {
                // KakaoMap 의 tag 을 설정
                return "FirstMapTag"
            }
        })
    }
}