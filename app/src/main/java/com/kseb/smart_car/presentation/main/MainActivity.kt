package com.kseb.smart_car.presentation.main

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.kakaomobility.knsdk.common.gps.WGS84ToKATEC
import com.kakaomobility.knsdk.common.util.FloatPoint
import com.kakaomobility.knsdk.map.knmaprenderer.objects.KNMapCameraUpdate
import com.kakaomobility.knsdk.map.knmapview.KNMapView
import com.kakaomobility.knsdk.ui.view.KNNaviView
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.ActivityMainBinding
import com.kseb.smart_car.presentation.main.music.MusicFragment
import com.kseb.smart_car.presentation.main.my.MyFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val subjectViewModel: SubjectViewModel by viewModels()
    private lateinit var mapView: KNMapView
    private lateinit var knNaviView: KNNaviView

    //프래그먼트 켜져있는지 체크하는 변수
    private var isMyFragmentVisible = false
    private var isMusicFragmentVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinds()
        setting()
        clickButtonNavigation()
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

    //마이페이지
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fcv_main, fragment)
//            .addToBackStack(null)
            .commit()
        binding.btnCurrentLocation.visibility = View.INVISIBLE
    }

    private fun removeAllFragments() {
        val fragmentManager = supportFragmentManager
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val currentFragment = fragmentManager.findFragmentById(R.id.fcv_main)
        currentFragment?.let {
            fragmentManager.beginTransaction().remove(it).commitNow()
        }
    }

//    private fun updateButtonColors(selectedItemId: Int) {
//        val menu = binding.bnvMain.menu
//        for (i in 0 until menu.size()) {
//            val item = menu.getItem(i)
//            val color = if (item.itemId == selectedItemId) {
//                ContextCompat.getColor(this, R.color.bnv_clicked_pink)
//            } else {
//                ContextCompat.getColor(this, R.color.bnv_unclicked_grey)
//            }
//            item.icon?.setTint(color)
//        }
//    }

    private fun clickButtonNavigation() {
        binding.bnvMain.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.menu_map -> {
                    removeAllFragments()
                    binding.btnCurrentLocation.visibility = View.VISIBLE
                    isMyFragmentVisible = false
                    isMusicFragmentVisible = false
                    true
                }

                R.id.menu_my -> {
                    removeAllFragments()
                    if (isMyFragmentVisible) {
                        isMyFragmentVisible = false
//                        binding.bnvMain.selectedItemId = R.id.menu_map
//                        updateButtonColors(R.id.menu_map)
                    } else {
                        replaceFragment(MyFragment())
                        isMyFragmentVisible = true
                    }
                    isMusicFragmentVisible = false
                    true
                }
                R.id.menu_music -> {
                    removeAllFragments()
                    if (isMusicFragmentVisible) {
                        isMusicFragmentVisible = false
//                        binding.bnvMain.selectedItemId = R.id.menu_map
//                        updateButtonColors(R.id.menu_map)
                    } else {
                        replaceFragment(MusicFragment())
                        isMusicFragmentVisible = true
                    }
                    isMyFragmentVisible = false
                    true
                }
                else -> {
                    false
                }
            }
        }
    }
}