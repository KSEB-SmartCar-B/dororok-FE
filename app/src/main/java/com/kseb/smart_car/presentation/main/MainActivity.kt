package com.kseb.smart_car.presentation.main

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.kakaomobility.knsdk.KNCarFuel
import com.kakaomobility.knsdk.KNCarType
import com.kakaomobility.knsdk.KNCarUsage
import com.kakaomobility.knsdk.KNRouteAvoidOption
import com.kakaomobility.knsdk.KNRoutePriority
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.common.gps.WGS84ToKATEC
import com.kakaomobility.knsdk.common.objects.KNError
import com.kakaomobility.knsdk.common.objects.KNPOI
import com.kakaomobility.knsdk.common.util.FloatPoint
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_CitsGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_GuideStateDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_LocationGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_RouteGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_SafetyGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_VoiceGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuideRouteChangeReason
import com.kakaomobility.knsdk.guidance.knguidance.KNGuideState
import com.kakaomobility.knsdk.guidance.knguidance.citsguide.KNGuide_Cits
import com.kakaomobility.knsdk.guidance.knguidance.common.KNLocation
import com.kakaomobility.knsdk.guidance.knguidance.locationguide.KNGuide_Location
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.KNGuide_Route
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.objects.KNMultiRouteInfo
import com.kakaomobility.knsdk.guidance.knguidance.safetyguide.KNGuide_Safety
import com.kakaomobility.knsdk.guidance.knguidance.safetyguide.objects.KNSafety
import com.kakaomobility.knsdk.guidance.knguidance.voiceguide.KNGuide_Voice
import com.kakaomobility.knsdk.map.knmaprenderer.objects.KNMapCameraUpdate
import com.kakaomobility.knsdk.map.knmapview.KNMapView
import com.kakaomobility.knsdk.trip.knrouteconfiguration.KNRouteConfiguration
import com.kakaomobility.knsdk.trip.kntrip.KNTrip
import com.kakaomobility.knsdk.trip.kntrip.knroute.KNRoute
import com.kakaomobility.knsdk.ui.view.KNNaviView
import com.kakaomobility.knsdk.ui.view.KNNaviView_GuideStateDelegate
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.ActivityMainBinding
import com.kseb.smart_car.presentation.MyApp
import com.kseb.smart_car.presentation.main.music.MusicFragment
import com.kseb.smart_car.presentation.main.my.MyFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlin.properties.Delegates


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), KNGuidance_GuideStateDelegate,
    KNGuidance_LocationGuideDelegate, KNGuidance_RouteGuideDelegate,
    KNGuidance_SafetyGuideDelegate, KNGuidance_VoiceGuideDelegate, KNGuidance_CitsGuideDelegate {
    private lateinit var binding: ActivityMainBinding
    private val subjectViewModel: SubjectViewModel by viewModels()
    private val mainViewModel:MainViewModel by viewModels()
    private lateinit var updateReceiver: BroadcastReceiver

    private lateinit var mapView: KNMapView
    private lateinit var knNaviView: KNNaviView

    private var currentLongitude by Delegates.notNull<Double>()
    private var currentLatitude by Delegates.notNull<Double>()

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

        currentLongitude = intent.getDoubleExtra("longitude", 0.0)
        currentLatitude = intent.getDoubleExtra("latitude", 0.0)

        mainViewModel.setAccessToken(intent.getStringExtra("accessToken")!!)
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

        binding.btnSearch.setOnClickListener {
            getDirections()
        }

        // BroadcastReceiver 초기화 및 등록
        updateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action
                if (action == "com.example.UPDATE_INFO") {
                    Handler(Looper.getMainLooper()).post {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fcv_main, MyFragment())
                            .commitAllowingStateLoss()
                    }
                }
            }
        }


        LocalBroadcastManager.getInstance(this).registerReceiver(
            updateReceiver, IntentFilter("com.example.UPDATE_INFO")
        )
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

        Log.d("mainactivity", "wgs84-long:${currentLongitude}, lati:${currentLatitude}")
        val coordinate = WGS84ToKATEC(currentLongitude, currentLatitude)
        mapView.moveCamera(positionWithKNMapCameraUpdate(coordinate.toFloatPoint()), false)
        Log.d("mainactivity", "float long:${coordinate.x}}, latitu:${coordinate.y}")

    }

    private fun positionWithKNMapCameraUpdate(coordinate: FloatPoint): KNMapCameraUpdate {
        return KNMapCameraUpdate.targetTo(coordinate)
    }

    private fun getDirections() {
        //위도 : 37.28682370552076
        //경도 : 126.57606547684927
        val currentKatec = WGS84ToKATEC(currentLongitude, currentLatitude)
        val goalKatec = WGS84ToKATEC(126.99985062, 37.26609306)
        Log.d(
            "mainactivity",
            "long:${currentKatec.x}, lati:${currentKatec.y}\n goal:${goalKatec.x} - ${goalKatec.y}"
        )

        val start = KNPOI("current", currentKatec.x.toInt(), currentKatec.y.toInt())
        val goal = KNPOI("sea", goalKatec.x.toInt(), goalKatec.y.toInt())
        // 경로 생성
        KNSDK.makeTripWithStart(start, goal, null) { knError, knTrip ->
            if (knError != null) {
                // 오류 처리
                println("경로 생성 실패: ${knError.msg}")
                // 여기에서 사용자에게 오류를 알리거나 로그를 남기는 작업을 수행합니다.
            } else if (knTrip != null) {
                // 성공 처리
                println("경로 생성 성공: 시작점 - ${knTrip.start}, 도착점 - ${knTrip.goal}")
                // 차량 정보 및 기타 설정 반영
                val routeConfig = KNRouteConfiguration(
                    KNCarType.KNCarType_1,      // 차량의 종류 (예: SEDAN, SUV 등)
                    KNCarFuel.KNCarFuel_Gasoline,  // 유고 정보 반영 여부
                    true,                 // 하이패스 장착 여부
                    KNCarUsage.KNCarUsage_Default,     // 차량의 용도 (예: PERSONAL, COMMERCIAL 등)
                    -1,                 // 차량의 전폭 (단위: mm)
                    -1,                 // 차량의 전고 (단위: mm)
                    -1,                 // 차량의 전장 (단위: mm)
                    -1                  // 차량의 중량 (단위: kg)
                )

                // 경로에 설정 적용
                knTrip.setRouteConfig(routeConfig)

                // 경로 옵션 설정
                val curRoutePriority = KNRoutePriority.KNRoutePriority_Recommand
                val curAvoidOptions = 0

                Log.d("mainactivity", "우선순위: ${curRoutePriority}, 회피옵션: ${curAvoidOptions}")

                // 경로 요청
                knTrip.routeWithPriority(curRoutePriority, curAvoidOptions) { error, _ ->
                    if (error != null) {
                        // 경로 요청 실패
                        Log.e(
                            "mainactivity",
                            "경로 요청 실패: code - ${error.code}, msg - ${error.msg} \n ${error.tagMsg} === ${error.extra}"
                        )
                    } else {
                        // 경로 요청 성공
                        Log.d("mainActivity","경로 요청 성공!")
                        val naviView = KNSDK.sharedGuidance()
                        naviView?.apply {
                            // 각 가이던스 델리게이트 등록
                            guideStateDelegate = this@MainActivity
                            locationGuideDelegate = this@MainActivity
                            routeGuideDelegate = this@MainActivity
                            safetyGuideDelegate = this@MainActivity
                            voiceGuideDelegate = this@MainActivity
                            citsGuideDelegate = this@MainActivity

                            knNaviView.initWithGuidance(
                                this,
                                knTrip,
                                curRoutePriority,
                                curAvoidOptions
                            )
                        }
                    }
                }

            } else {
                // 예상치 못한 상태 처리
                println("예상치 못한 오류 발생")
            }
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
            when (it.itemId) {
                R.id.menu_map -> {
                    removeAllFragments()
                    binding.btnCurrentLocation.visibility = View.VISIBLE
                    true
                }

                R.id.menu_my -> {
                    replaceFragment(MyFragment())
                    true
                }

                R.id.menu_music -> {
                    replaceFragment(MusicFragment())
                    true
                }

                else -> {
                    false
                }
            }
        }
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

    // 길 안내 시작 시 호출
    override fun guidanceGuideStarted(aGuidance: KNGuidance) {
        knNaviView.guidanceGuideStarted(aGuidance)
    }

    // 경로 변경 시 호출. 교통 변화 또는 경로 이탈로 인한 재탐색 및 사용자 재탐색 시 전달
    override fun guidanceCheckingRouteChange(aGuidance: KNGuidance) {
        knNaviView.guidanceCheckingRouteChange(aGuidance)
    }

    // 수신 받은 새 경로가 기존의 안내된 경로와 동일할 경우 호출
    override fun guidanceRouteUnchanged(aGuidance: KNGuidance) {
        knNaviView.guidanceRouteUnchanged(aGuidance)
    }

    // 경로에 오류가 발생 시 호출
    override fun guidanceRouteUnchangedWithError(aGuidnace: KNGuidance, aError: KNError) {
        knNaviView.guidanceRouteUnchangedWithError(aGuidnace, aError)
    }

    // 경로에서 이탈한 뒤 새로운 경로를 요청할 때 호출
    override fun guidanceOutOfRoute(aGuidance: KNGuidance) {
        knNaviView.guidanceOutOfRoute(aGuidance)
    }

    // 수신 받은 새 경로가 기존의 안내된 경로와 다를 경우 호출. 여러 개의 경로가 있을 경우 첫 번째 경로를 주행 경로로 사용하고 나머지는 대안 경로로 설정됨
    override fun guidanceRouteChanged(
        aGuidance: KNGuidance,
        aFromRoute: KNRoute,
        aFromLocation: KNLocation,
        aToRoute: KNRoute,
        aToLocation: KNLocation,
        aChangeReason: KNGuideRouteChangeReason
    ) {
        knNaviView.guidanceRouteChanged(aGuidance)
    }

    // 길 안내 종료 시 호출
    override fun guidanceGuideEnded(aGuidance: KNGuidance) {
        knNaviView.guidanceGuideEnded(aGuidance)
    }

    // 주행 중 기타 요인들로 인해 경로가 변경되었을 때 호출
    override fun guidanceDidUpdateRoutes(
        aGuidance: KNGuidance,
        aRoutes: List<KNRoute>,
        aMultiRouteInfo: KNMultiRouteInfo?
    ) {
        knNaviView.guidanceDidUpdateRoutes(aGuidance, aRoutes, aMultiRouteInfo)
    }

    // KNGuidance_LocationGuideDelegate

    // 위치 정보가 변경될 경우 호출. `locationGuide`의 항목이 1개 이상 변경 시 전달됨.
    override fun guidanceDidUpdateLocation(
        aGuidance: KNGuidance,
        aLocationGuide: KNGuide_Location
    ) {
        knNaviView.guidanceDidUpdateLocation(aGuidance,aLocationGuide)
    }

    // KNGuidance_RouteGuideDelegate

    // 경로 안내 정보 업데이트 시 호출. `routeGuide`의 항목이 1개 이상 변경 시 전달됨.
    override fun guidanceDidUpdateRouteGuide(aGuidance: KNGuidance, aRouteGuide: KNGuide_Route) {
        knNaviView.guidanceDidUpdateRouteGuide(aGuidance,aRouteGuide)
    }

    // KNGuidance_SafetyGuideDelegate

    // 안전 운행 정보 업데이트 시 호출. `safetyGuide`의 항목이 1개 이상 변경 시 전달됨.
    override fun guidanceDidUpdateSafetyGuide(
        aGuidance: KNGuidance,
        aSafetyGuide: KNGuide_Safety?
    ) {
        knNaviView.guidanceDidUpdateSafetyGuide(aGuidance,aSafetyGuide)
    }

    // 주변의 안전 운행 정보 업데이트 시 호출
    override fun guidanceDidUpdateAroundSafeties(
        aGuidance: KNGuidance,
        aSafeties: List<KNSafety>?
    ) {
        knNaviView.guidanceDidUpdateAroundSafeties(aGuidance, aSafeties)
    }

    // KNGuidance_VoiceGuideDelegate

    // 음성 안내 사용 여부
    override fun shouldPlayVoiceGuide(
        aGuidance: KNGuidance,
        aVoiceGuide: KNGuide_Voice,
        aNewData: MutableList<ByteArray>
    ): Boolean {
        return knNaviView.shouldPlayVoiceGuide(aGuidance, aVoiceGuide, aNewData)
    }

    // 음성 안내 시작
    override fun willPlayVoiceGuide(aGuidance: KNGuidance, aVoiceGuide: KNGuide_Voice) {
        knNaviView.willPlayVoiceGuide(aGuidance, aVoiceGuide)
    }

    // 음성 안내 종료
    override fun didFinishPlayVoiceGuide(aGuidance: KNGuidance, aVoiceGuide: KNGuide_Voice) {
        knNaviView.didFinishPlayVoiceGuide(aGuidance, aVoiceGuide)
    }

    // KNGuidance_CitsGuideDelegate

    // CITS 정보 변경 시 호출
    override fun didUpdateCitsGuide(aGuidance: KNGuidance, aCitsGuide: KNGuide_Cits) {
        knNaviView.didUpdateCitsGuide(aGuidance, aCitsGuide)
    }

    override fun onDestroy() {
        super.onDestroy()
        // BroadcastReceiver 해제
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver)
    }
}