package com.kseb.smart_car.presentation.main

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.kakaomobility.knsdk.common.gps.WGS84ToKATEC
import com.kakaomobility.knsdk.common.util.FloatPoint
import com.kakaomobility.knsdk.map.knmaprenderer.objects.KNMapCameraUpdate
import com.kakaomobility.knsdk.map.knmapview.KNMapView
import com.kakaomobility.knsdk.ui.view.KNNaviView
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val subjectViewModel: SubjectViewModel by viewModels()
    private lateinit var mapView: KNMapView
    private lateinit var knNaviView: KNNaviView

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

    // 카메라를 현재 위치로 이동
    private fun showCurrentLocation() {

        val longitude=intent.getDoubleExtra("longitude",0.0)
        val latitude=intent.getDoubleExtra("latitude",0.0)
        Log.d("mainactivity","wgs84-long:${longitude}, lati:${latitude}")
        val coordinate= WGS84ToKATEC(longitude,latitude)
        mapView.moveCamera(positionWithKNMapCameraUpdate(coordinate.toFloatPoint()), false)
        Log.d("mainactivity","float long:${coordinate.x}}, latitu:${coordinate.y}")

    }

    private fun positionWithKNMapCameraUpdate(coordinate: FloatPoint): KNMapCameraUpdate {
        return KNMapCameraUpdate.targetTo(coordinate)
    }
}