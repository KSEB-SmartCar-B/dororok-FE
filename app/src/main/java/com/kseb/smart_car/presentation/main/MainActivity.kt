package com.kseb.smart_car.presentation.main

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kakao.sdk.common.KakaoSdk.appKey
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapGravity
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakaomobility.knsdk.KNLanguageType
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.common.gps.KATECToWGS84
import com.kakaomobility.knsdk.common.objects.KNError_Code_C302
import com.kakaomobility.knsdk.common.util.FloatPoint
import com.kakaomobility.knsdk.map.knmaprenderer.objects.KNMapCameraUpdate
import com.kakaomobility.knsdk.ui.view.KNNaviView
import com.kseb.smart_car.BuildConfig
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.ActivityMainBinding
import com.kseb.smart_car.extension.AddressState
import com.kseb.smart_car.presentation.main.MainActivity.SpotifySampleContexts.TRACK_URI
import com.kseb.smart_car.presentation.main.music.MusicFragment
import com.kseb.smart_car.presentation.main.music.PlayFragment.AuthParams.CLIENT_ID
import com.kseb.smart_car.presentation.main.music.PlayFragment.AuthParams.REDIRECT_URI
import com.kseb.smart_car.presentation.main.music.PlayFragment.Companion.TAG
import com.kseb.smart_car.presentation.main.music.SituationAdapter
import com.kseb.smart_car.presentation.main.music.SituationViewModel
import com.kseb.smart_car.presentation.main.my.MyFragment
import com.kseb.smart_car.presentation.main.navi.NaviActivity
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.SpotifyDisconnectedException
import com.kseb.smart_car.presentation.main.search.SearchActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.roundToInt
import kotlin.properties.Delegates


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val subjectViewModel: SubjectViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private val situationViewModel: SituationViewModel by viewModels()

    private var spotifyAppRemote: SpotifyAppRemote? = null
    private val errorCallback = { throwable: Throwable -> logError(throwable) }

    private lateinit var updateReceiver: BroadcastReceiver

    private lateinit var kakaoMapView: MapView
    private lateinit var knNaviView: KNNaviView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_CODE_LOCATION_PERMISSION = 123

    private var currentLongitude by Delegates.notNull<Double>()
    private var currentLatitude by Delegates.notNull<Double>()
    private var currentPosition: LatLng? = null
    val PERMISSIONS_REQUEST_CODE = 100
    var REQUIRED_PERMISSIONS = arrayOf<String>(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    private var isPlay = false

    object AuthParams {
        const val CLIENT_ID = "d8e2d4268f28445eac8333a5292c8e9f"
        const val REDIRECT_URI = "https://com.kseb.smart_car/callback"
    }

    object SpotifySampleContexts {
        const val TRACK_URI = "spotify:track:5sdQOyqq2IDhvmx2lHOpwd"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinds()
        setting()
        clickButtonNavigation()
        clickButtonSearch()
    }

    private fun initBinds() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setting() {
        setWindowTransparent()

        //accessToken 받아옴
        mainViewModel.setAccessToken(intent.getStringExtra("accessToken")!!)

        //맵 초기화
        kakaoMapView = MapView(this)
        binding.mapView.addView(kakaoMapView)
        setKakaoMap()

        //검색창
        initSearchView()

        //상단 버튼 만들기
        val subjectAdapter = SubjectAdapter()
        // binding.rvSubject.adapter = subjectAdapter
        subjectAdapter.getList(subjectViewModel.makeList())
        // binding.btnMusic.visibility=View.INVISIBLE

        binding.btnSearch.setOnClickListener {
            //내비 초기화
            setKakaoNavi()
        }

        clickMusicButton()

        // BroadcastReceiver 초기화 및 등록 (마이페이지에서 정보 수정 시 사용)
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

    private fun setWindowTransparent() {
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
    }

    private fun getNavigationBarHeight(): Int {
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")

        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId)
        else 0
    }

    private fun setKakaoMap() {
        kakaoMapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                // 지도 API 가 정상적으로 종료될 때 호출됨
            }

            override fun onMapError(error: Exception) {
                // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출됨
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                // 인증 후 API 가 정상적으로 실행될 때 호출됨
                Log.d("mainactivity", "mapready")
                val compass = kakaoMap.compass
                compass!!.setPosition(MapGravity.TOP or MapGravity.LEFT, 100f, 150f)
                compass.setBackToNorthOnClick(true)
                compass.show()

                CoroutineScope(Dispatchers.IO).launch {
                    getMyLocation(this@MainActivity, kakaoMap)
                    getPosition()
                    kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(currentPosition!!))
                }

                binding.btnCurrentLocation.setOnClickListener {
                    kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(currentPosition!!))
                }
            }

            private fun getMyLocation(context: Context, kakaoMap: KakaoMap) {
                val permissionCheck = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    Log.d("mainactivity", "permission_granted")
                    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    try {
                        val userCurLocation =
                            lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        val uLatitude = userCurLocation!!.latitude
                        val uLogitude = userCurLocation.longitude
                        Log.d("mainactivity", "latitude: ${uLatitude}, logitude: ${uLogitude}")
                        currentPosition = LatLng.from(uLatitude, uLogitude)

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
                    ActivityCompat.requestPermissions(
                        context as Activity,
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

    private fun setKakaoNavi() {
        knNaviView = KNNaviView(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocation()
    }

    private fun initializeKNSDK(longitude: Double, latitude: Double) {
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
                    Log.d("mainActivity", "인증완료")
                    Log.d("mainActivity", "initialize -> 현재좌표: ${longitude}, ${latitude}")
                    val intent = Intent(this, NaviActivity::class.java)
                    intent.putExtra("currentLongitude", longitude)
                    intent.putExtra("currentLatitude", latitude)
                    startActivity(intent)
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
                    currentLongitude = location.longitude
                    currentLatitude = location.latitude
                    Log.d("mainActivity", "long:${currentLongitude}, lati:${currentLatitude}")
                    initializeKNSDK(currentLongitude, currentLatitude)
                    // KNNaviView에 위치 정보 전달
                    /* knNaviView.mapComponent.mapView.getMapToCenter().let { pos ->
                         val point = KATECToWGS84(
                             pos.x.roundToInt().toDouble(),
                             pos.y.roundToInt().toDouble()
                         )*/
                    /*// 위치 정보를 보여주는 다이얼로그
                    AlertDialog.Builder(this)
                        .setTitle("현재 위치")
                        .setMessage("longitude: ${point.x}\nlatitude: ${point.y}")
                        .setCancelable(true)
                        .setNegativeButton("close") { dialog, _ -> dialog.dismiss() }
                        .create()
                        .show()*/


                    // 위치 정보를 사용하여 다음 Activity로 이동
                    /*val token=intent.getStringExtra("accessToken")
                    Log.d("locationActivity","token:${token}")
                    val intent = Intent(this, NaviActivity::class.java)
                    intent.putExtra("accessToken",token)
                    intent.putExtra("longitude", currentLongitude)
                    intent.putExtra("latitude", currentLatitude)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()*/
                    //}
                } else {
                    Log.e("mainActivity", "Location is null")
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

    private fun initSearchView() {
        // init SearchView
        binding.svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // @TODO
                if (newText != null) {
                    mainViewModel.getAddress(newText)
                    lifecycleScope.launch {
                        mainViewModel.addressState.collect { addressState ->
                            when (addressState) {
                                is AddressState.Success -> {
                                    Log.d("mainActivity", "주소 가져오기 성공!")
                                    Log.d(
                                        "mainActivity",
                                        "${addressState.addressDto.documents[0].placeName}"
                                    )
                                }

                                is AddressState.Loading -> {}
                                is AddressState.Error -> {
                                    Log.e("mainActivity", "주소 가져오기 에러!")
                                }
                            }
                        }
                    }
                }
                return true
            }
        })
    }


    // 카메라를 현재 위치로 이동
    /*private fun showCurrentLocation() {
        mapView = binding.naviView.mapComponent.mapView
        if (currentLongitude == null || currentLatitude == null) {
            Log.e("mainactivity", "현재 위치가 유효하지 않습니다.")
            return
        }

        Log.d(
            "mainactivity",
            "WGS84 - Longitude: $currentLongitude, Latitude: $currentLatitude"
        )

        try {
            val coordinate = WGS84ToKATEC(currentLongitude, currentLatitude)
            mapView.moveCamera(positionWithKNMapCameraUpdate(coordinate.toFloatPoint()), true)
            Log.d(
                "mainactivity",
                "Converted - Longitude: ${coordinate.x}, Latitude: ${coordinate.y}"
            )
        } catch (e: ClassNotFoundException) {
            Log.e("mainactivity", "ClassNotFoundException: ${e.message}")
        } catch (e: NoClassDefFoundError) {
            Log.e("mainactivity", "NoClassDefFoundError: ${e.message}")
        }
    }*/


    private fun positionWithKNMapCameraUpdate(coordinate: FloatPoint): KNMapCameraUpdate {
        return KNMapCameraUpdate.targetTo(coordinate)
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

    private fun clickMusicButton() {
        binding.btnMusic.setOnClickListener {
            if (binding.rvSituation.visibility == View.GONE) {
                binding.rvSituation.visibility = View.VISIBLE
            } else {
                binding.rvSituation.visibility = View.GONE
            }

            if (isPlay) {
                isPlay = false
                Log.d("mainActivity", "isPlay true")
                onPlayPauseButtonClicked()
            } else {
                val situationAdapter =
                    SituationAdapter(this, { situation -> onItemClicked(situation) }, "main")
                binding.rvSituation.adapter = situationAdapter
                situationAdapter.getList(situationViewModel.makeList())
                Log.d("mainActivity", "isPlay false")
            }
        }
    }

    private fun onItemClicked(situation: String) {
        SpotifyAppRemote.disconnect(spotifyAppRemote)
        binding.rvSituation.visibility = View.INVISIBLE
        Log.d("mainactivity", "음악 클릭!")
        lifecycleScope.launch {
            try {
                spotifyAppRemote = connectToAppRemote()
                playUri(TRACK_URI) // onItemClicked에서 연결 후 재생 시도
            } catch (error: Throwable) {
                logError(error)
            }
        }
    }

    private fun clickButtonSearch() {
        binding.svSearch.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private suspend fun connectToAppRemote(): SpotifyAppRemote? =
        suspendCoroutine { cont: Continuation<SpotifyAppRemote> ->
            SpotifyAppRemote.connect(
                this.application,
                ConnectionParams.Builder(CLIENT_ID)
                    .setRedirectUri(REDIRECT_URI)
                    .showAuthView(true)
                    .build(),
                object : Connector.ConnectionListener {
                    override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                        Log.d("connec", "onConnected 실행!")
                        cont.resume(spotifyAppRemote)
                    }

                    override fun onFailure(error: Throwable) {
                        Log.e("mainActivity", "connect fail!", error)
                        cont.resumeWithException(error)
                    }
                })
        }

    private fun playUri(uri: String) {
        Log.d("mainactivity", "playuri실행!")
        try {
            assertAppRemoteConnected()
                .playerApi
                .play(uri)
                .setResultCallback {
                    logMessage(
                        getString(
                            R.string.command_feedback,
                            "play"
                        )
                    )
                }
                .setErrorCallback { error -> logError(error) }
            onPlayPauseButtonClicked()
        } catch (e: SpotifyDisconnectedException) {
            logError(e)
            //reconnectAndPlay(uri) // 재연결 후 재생 시도
        }
    }

    private fun assertAppRemoteConnected(): SpotifyAppRemote {
        Log.d("mainActivity", "assertAppRemoteConnected 실행!")
        return spotifyAppRemote?.takeIf { it.isConnected } ?: run {
            Log.e(TAG, getString(R.string.err_spotify_disconnected))
            throw SpotifyDisconnectedException()
        }
    }

    /*private fun reconnectAndPlay(uri: String) {
        lifecycleScope.launch {
            try {
                spotifyAppRemote = connectToAppRemote()
                playUri(uri) // 재연결 후 재생 시도
                onPlayPauseButtonClicked()
            } catch (error: Throwable) {
                logError(error)
            }
        }
    }*/

    private fun onPlayPauseButtonClicked() {
        assertAppRemoteConnected().let {
            it.playerApi
                .playerState
                .setResultCallback { playerState ->
                    if (playerState.isPaused) {
                        it.playerApi
                            .resume()
                            .setResultCallback {
                                logMessage(
                                    getString(
                                        R.string.command_feedback,
                                        "play"
                                    )
                                )
                            }
                            .setErrorCallback(errorCallback)
                        isPlay = true
                    } else {
                        it.playerApi
                            .pause()
                            .setResultCallback {
                                logMessage(
                                    getString(
                                        R.string.command_feedback,
                                        "pause"
                                    )
                                )
                            }
                            .setErrorCallback(errorCallback)
                        isPlay = false
                    }
                }
        }
    }

    private fun logError(throwable: Throwable) {
        Toast.makeText(this, R.string.err_generic_toast, Toast.LENGTH_SHORT).show()
        Log.e(TAG, "", throwable)
    }

    private fun logMessage(msg: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, msg, duration).show()
        Log.d(TAG, msg)
    }

    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(this).setTitle(title).setMessage(message).create().show()

    }

    //메인 액티비티에서 searchview 타자 보이지 않도록 설정
    override fun onResume() {
        super.onResume()
        binding.svSearch.clearFocus()
    }

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
        fragmentManager.popBackStackImmediate(
            null,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        val currentFragment = fragmentManager.findFragmentById(R.id.fcv_main)
        currentFragment?.let {
            fragmentManager.beginTransaction().remove(it).commitNow()
        }
    }
}