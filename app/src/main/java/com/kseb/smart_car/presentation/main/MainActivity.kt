package com.kseb.smart_car.presentation.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.common.gps.KATECToWGS84
import com.kakaomobility.knsdk.common.objects.KNError
import com.kakaomobility.knsdk.common.util.DoublePoint
import com.kakaomobility.knsdk.map.knmaprenderer.objects.KNMapCameraUpdate
import com.kakaomobility.knsdk.map.knmapview.KNMapView
import com.kakaomobility.knsdk.map.uicustomsupport.renewal.KNMapMarker
import com.kakaomobility.knsdk.map.uicustomsupport.renewal.KNMapUserLocation
import com.kakaomobility.knsdk.map.uicustomsupport.renewal.theme.base.KNMapTheme
import com.kakaomobility.knsdk.ui.view.KNNaviView
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val subjectViewModel: SubjectViewModel by viewModels()
    private lateinit var mapView: KNMapView
    private lateinit var knNaviView: KNNaviView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var knMapCameraUpdate: KNMapCameraUpdate
    private var withUserLocation = false

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
        mapView = binding.naviView.mapComponent.mapView
        //상태바 투명하게
        val window = window
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        window.navigationBarColor = ContextCompat.getColor(this, R.color.system_bnv_grey)

        //시스템 하단바의 높이 만큼 하단네비게이션바를 올림
        val bnvMain = binding.bnvMain
        val layoutParams = bnvMain.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.bottomMargin = getNavigationBarHeight()
        bnvMain.layoutParams = layoutParams

        //검색창
        initSearchView()

        //상단 버튼 만들기
        val subjectAdapter = SubjectAdapter()
        binding.rvSubject.adapter = subjectAdapter
        subjectAdapter.getList(subjectViewModel.makeList())

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        knNaviView = binding.naviView
        //현재 위치 버튼 클릭 시
        binding.btnCurrentLocation.setOnClickListener {
            showCurrentLocation()
        }

    }

    private fun getNavigationBarHeight(): Int {
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")

        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId)
        else 0
    }

    private fun initSearchView() {
        // init SearchView
        binding.svSearch.isSubmitButtonEnabled = true
        binding.svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // @TODO
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // @TODO
                return true
            }
        })
    }

    private fun showCurrentLocation() {
        // 카메라를 현재 위치로 이동
        bindMapView {
            if (it == null) return@bindMapView
        }

        // 사용자의 현재 위치로 카메라를 이동시킵니다.
        mapView.moveCamera(KNMapCameraUpdate.targetTo(mapView.userLocation!!.coordinate), true)
        // 현재 위치에 마커 표시
        KNMapMarker(mapView.coordinate).apply {
            tag = 1
            priority = 1
            //setVisigleRange(.1f,30f)
            mapView.addMarker(this)
        }
    }

    fun bindMapView(completion: ((KNError?) -> Unit)?) {
        KNSDK.bindingMapView(
            mapView,
            KNMapTheme.driveDay()
        ) { error ->
            completion?.invoke(error)
            if (error != null) {
                Toast.makeText(
                    this,
                    "맵 초기화 작업이 실패하였습니다. \n[${error.code}] : ${error.msg}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("mainactivity","code: ${error.code}, msg: ${error.msg}")
                return@bindingMapView
            }
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}