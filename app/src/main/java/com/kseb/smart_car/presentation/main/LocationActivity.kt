package com.kseb.smart_car.presentation.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kakao.sdk.common.KakaoSdk.appKey
import com.kakaomobility.knsdk.KNLanguageType
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.common.gps.KATECToWGS84
import com.kakaomobility.knsdk.common.objects.KNError_Code_C302
import com.kakaomobility.knsdk.ui.view.KNNaviView
import com.kseb.smart_car.BuildConfig
import com.kseb.smart_car.databinding.ActivityLocationBinding
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class LocationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLocationBinding

    private lateinit var knNaviView: KNNaviView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_CODE_LOCATION_PERMISSION = 123

    private var longitude by Delegates.notNull<Double>()
    private var latitude by Delegates.notNull<Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        knNaviView = binding.naviView
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initializeKNSDK()
    }

    private fun initializeKNSDK() {
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
                    checkLocation()
                }
            })
    }

    private fun checkLocation() {
        // 위치 권한이 이미 허용되어 있는지 확인
        if (isLocationPermissionGranted()) {
            // 위치 기능 사용 가능
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 권한이 없는 경우 처리
                requestLocationPermission()
                return
            }

            // 위치 요청
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    // 위치 정보를 가져온 후 처리
                    longitude = location.longitude
                    latitude = location.latitude
                    Log.d("locationactivity", "long:${longitude}, lati:${latitude}")

                    // KNNaviView에 위치 정보 전달
                    knNaviView.mapComponent.mapView.getMapToCenter().let { pos ->
                        val point = KATECToWGS84(
                            pos.x.roundToInt().toDouble(),
                            pos.y.roundToInt().toDouble()
                        )
                        /*// 위치 정보를 보여주는 다이얼로그
                        AlertDialog.Builder(this)
                            .setTitle("현재 위치")
                            .setMessage("longitude: ${point.x}\nlatitude: ${point.y}")
                            .setCancelable(true)
                            .setNegativeButton("close") { dialog, _ -> dialog.dismiss() }
                            .create()
                            .show()*/

                        // 위치 정보를 사용하여 다음 Activity로 이동
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("longitude", longitude)
                        intent.putExtra("latitude", latitude)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Log.e("locationactivity", "Location is null")
                }
            }
        } else {
            // 위치 권한 요청
            requestLocationPermission()
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_CODE_LOCATION_PERMISSION
        )
        checkLocation()
    }


}