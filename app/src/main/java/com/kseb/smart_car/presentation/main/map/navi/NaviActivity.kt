package com.kseb.smart_car.presentation.main.map.navi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.kakaomobility.knsdk.KNCarFuel
import com.kakaomobility.knsdk.KNCarType
import com.kakaomobility.knsdk.KNCarUsage
import com.kakaomobility.knsdk.KNRoutePriority
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.common.gps.WGS84ToKATEC
import com.kakaomobility.knsdk.common.objects.KNError
import com.kakaomobility.knsdk.common.objects.KNPOI
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
import com.kakaomobility.knsdk.map.knmapview.KNMapView
import com.kakaomobility.knsdk.trip.knrouteconfiguration.KNRouteConfiguration
import com.kakaomobility.knsdk.trip.kntrip.knroute.KNRoute
import com.kakaomobility.knsdk.ui.view.KNNaviView
import com.kakaomobility.knsdk.ui.view.KNNaviView_GuideStateDelegate
import com.kseb.smart_car.R
import com.kseb.smart_car.data.responseDto.ResponseRecommendMusicDto
import com.kseb.smart_car.databinding.ActivityNaviBinding
import com.kseb.smart_car.presentation.SpotifyRemoteManager
import com.kseb.smart_car.presentation.main.music.PlayFragment
import com.kseb.smart_car.presentation.main.music.PlayFragment.AuthParams.CLIENT_ID
import com.kseb.smart_car.presentation.main.music.PlayFragment.AuthParams.REDIRECT_URI
import com.kseb.smart_car.presentation.main.music.PlayFragment.SpotifySampleContexts.TRACK_URI
import com.kseb.smart_car.presentation.main.music.PlayViewModel
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.SpotifyDisconnectedException
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.Image
import com.spotify.protocol.types.PlayerContext
import com.spotify.protocol.types.PlayerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.properties.Delegates

@AndroidEntryPoint
class NaviActivity:AppCompatActivity(), KNGuidance_GuideStateDelegate,
    KNGuidance_LocationGuideDelegate, KNGuidance_RouteGuideDelegate,
    KNGuidance_SafetyGuideDelegate, KNGuidance_VoiceGuideDelegate,
    KNGuidance_CitsGuideDelegate, KNNaviView_GuideStateDelegate {
    private lateinit var binding:ActivityNaviBinding
    private lateinit var mapView: KNMapView
    private lateinit var knNaviView: KNNaviView
    private val naviView = KNSDK.sharedGuidance()

    private var currentLongitude by Delegates.notNull<Double>()
    private var currentLatitude by Delegates.notNull<Double>()
    private var placeName:String?=null

    private lateinit var speechRecognizer: SpeechRecognizer
    private var textToSpeech: TextToSpeech?=null
    var isTTSReady = false // TTS 준비 상태 플래그

    private val playViewModel: PlayViewModel by viewModels()

    private var hasCheckedSpotifyAppRemote = false
    private var spotifyAppRemote: SpotifyAppRemote? = null

    companion object {
        const val TAG = "Spotify"
        const val STEP_MS = 15000L

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private var playerStateSubscription: Subscription<PlayerState>? = null
    private var playerContextSubscription: Subscription<PlayerContext>? = null

    // 현재 재생 중인 곡의 인덱스
    private var currentTrackIndex = 0
    // 추천 음악 리스트
    private val recommendedMusicList = mutableListOf<ResponseRecommendMusicDto.RecommendMusicList>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBind()

        setting()
    }

    private fun initBind(){
        binding=ActivityNaviBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setting(){
        setWindowTransparent()

        mapView=binding.naviView.mapComponent.mapView
        knNaviView=binding.naviView
        knNaviView.guideStateDelegate = this

        currentLongitude = intent.getDoubleExtra("currentLongitude", 0.0)
        currentLatitude = intent.getDoubleExtra("currentLatitude", 0.0)
        placeName=intent.getStringExtra("placeName")
        val accessToken=intent.getStringExtra("accessToken")
        playViewModel.setAccessToken(accessToken!!)
        playViewModel.accessToken.observe(this){
            observeViewModel()
        }

        val constraintLayout = binding.root as ConstraintLayout
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        // `naviView`의 bottom을 parent의 bottom에 맞추고 margin을 설정합니다.
        constraintSet.connect(
            binding.naviView.id,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM
        )

        // bottomMargin 설정
        constraintSet.setMargin(
            binding.naviView.id,
            ConstraintSet.BOTTOM,
            getNavigationBarHeight()
        )

        constraintSet.applyTo(constraintLayout)

        getDirections()



        clickButton()
    }

    private fun setWindowTransparent(){
        //상태바 투명하게
        val window = window
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        window.navigationBarColor = ContextCompat.getColor(this, R.color.system_bnv_grey)
    }

    private fun getNavigationBarHeight(): Int {
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")

        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId)
        else 0
    }
    private fun getDirections() {
        //위도 : 37.28682370552076
        //경도 : 126.57606547684927
        Log.d("naviActivity","현재 좌표: ${currentLongitude}, ${currentLatitude }")
        val goalLongitude = intent.getDoubleExtra("goalLongitude", 0.0)
        val goalLatitude= intent.getDoubleExtra("goalLatitude", 0.0)
        val truncatedLongitude = String.format("%.6f", goalLongitude).toDouble()
        val truncatedLatitude = String.format("%.6f", goalLatitude).toDouble()
        val adjustedLongitude = truncatedLongitude + 0.0001 // 약간의 오프셋 추가
        val adjustedLatitude = truncatedLatitude + 0.0001 // 약간의 오프셋 추가

        val currentKatec = WGS84ToKATEC(currentLongitude, currentLatitude)
        val goalKatec = WGS84ToKATEC(adjustedLongitude, adjustedLatitude)
        Log.d("naviActivity", "Current: long:${currentKatec.x}, lati:${currentKatec.y}")
        Log.d("naviActivity", "Goal: long:${goalKatec.x}, lati:${goalKatec.y}")

        // 좌표 값 검증
        if (currentKatec.x.isNaN() || currentKatec.y.isNaN() || goalKatec.x.isNaN() || goalKatec.y.isNaN()) {
            Log.e("naviActivity", "Invalid coordinates detected. Aborting route request.")
            return
        }

        val start = KNPOI("current", currentKatec.x.toInt(), currentKatec.y.toInt())
        val goal = KNPOI(placeName!!, goalKatec.x.toInt(), goalKatec.y.toInt())
        // 경로 생성
        KNSDK.makeTripWithStart(start, goal, null) { knError, knTrip ->
            if (knError != null) {
                // 오류 처리
                Log.e("naviActivity","경로 생성 실패: ${knError.msg}")
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

                Log.d("naviActivity", "우선순위: ${curRoutePriority}, 회피옵션: ${curAvoidOptions}")

                // 경로 요청
                knTrip.routeWithPriority(curRoutePriority, curAvoidOptions) { error, _ ->
                    if (error != null) {
                        // 경로 요청 실패
                        Log.e(
                            "naviActivity",
                            "경로 요청 실패: code - ${error.code}, msg - ${error.msg} \n ${error.tagMsg} === ${error.extra}"
                        )
                    } else {
                        // 경로 요청 성공
                        Log.d("naviActivity", "경로 요청 성공!")
                        naviView?.apply {
                            // 각 가이던스 델리게이트 등록
                            guideStateDelegate = this@NaviActivity
                            locationGuideDelegate = this@NaviActivity
                            routeGuideDelegate = this@NaviActivity
                            safetyGuideDelegate = this@NaviActivity
                            voiceGuideDelegate = this@NaviActivity
                            citsGuideDelegate = this@NaviActivity

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

    //tts
    private fun clickButton(){
        binding.btnMusic.setOnClickListener{
            binding.btnMusic.isSelected=true
            //getRecommendMusic("일상")
            requestPermission() // 권한 요청 추가
            //tts 객체 초기화
            resetTTS()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this@NaviActivity).apply {
                setRecognitionListener(recognitionListener)
                // RecognizerIntent 생성
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_CALLING_PACKAGE,
                        this@NaviActivity.packageName
                    ) //여분의 키
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
                }
                // 여기에 startListening 호출 추가
                startListening(intent)
            }
        }
    }

    private fun requestPermission() {
        // 버전 체크, 권한 허용했는지 체크
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO), 0
            )
        }
    }

    private fun resetTTS(){
        // TTS 객체 초기화
        textToSpeech = TextToSpeech(this@NaviActivity) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.KOREAN)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // 언어 데이터가 없거나 지원하지 않는 언어일 때 처리
                } else {
                    isTTSReady = true // TTS가 준비되었음을 표시
                    textToSpeech?.setSpeechRate(2.0f) // TTS 속도 설정
                    speakInitialMessage() // 초기 메시지 음성 출력
                }
            } else {
                // TTS 초기화 실패 처리
            }
        }
    }

    private fun speakInitialMessage() {
        if(isTTSReady) {
            // 예제 메시지를 TTS로 말하기
            textToSpeech?.speak(this.getString(R.string.play_music), TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

    private val recognitionListener: RecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle) {
            Toast.makeText(this@NaviActivity, "이제 말씀하세요!", Toast.LENGTH_SHORT).show()
            //binding.tvState.text = "이제 말씀하세요!"
        }

        override fun onBeginningOfSpeech() {
            //binding.tvState.text = "잘 듣고 있어요."
            Log.d("searchfragment","onBeginningOfSpeech - 잘 듣고 있어요.")
        }

        // 입력받는 소리의 크기를 알려줌
        override fun onRmsChanged(rmsdB: Float) {}

        // 말을 시작하고 인식이 된 단어를 buffer에 담음
        override fun onBufferReceived(buffer: ByteArray) {}

        // 말하기를 중지하면 호출
        override fun onEndOfSpeech() {
            //binding.tvState.text = "끝!"
            Log.d("searchFragment","onendOfSpeech - 끝!")
            CoroutineScope(Dispatchers.Main).launch {
                delay(500)
                //binding.tvState.text="상태체크"
            }
        }

        // 오류 발생했을 때 호출
        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "오디오 에러"
                SpeechRecognizer.ERROR_CLIENT -> "클라이언트 에러"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "퍼미션 없음"
                SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트워크 타임아웃"
                SpeechRecognizer.ERROR_NO_MATCH -> "찾을 수 없음"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RECOGNIZER 가 바쁨"
                SpeechRecognizer.ERROR_SERVER -> "서버가 이상함"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "말하는 시간초과"
                else -> "알 수 없는 오류임"
            }
            // binding.tvState.text = "에러 발생: $message"
            Log.e("searchFragment","onError - error: ${message}")
        }

        override fun onResults(results: Bundle) {
            val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            binding.btnMusic.isSelected=false
            if (!matches.isNullOrEmpty()) {
                val text = matches[0] // 첫 번째 인식 결과를 사용
               when(text){
                   getString(R.string.daily),
                   getString(R.string.work),
                   getString(R.string.nowork),
                   getString(R.string.travel),
                   getString(R.string.drive),
                   getString(R.string.dororok),
                   getString(R.string.date),
                   getString(R.string.friend) -> {
                       getRecommendMusic(text)
                       Log.d("naviActivity", "${text}")
                   }
               }
                //messages.add(Message(text,MessageType.USER_INPUT)) // 인식된 텍스트를 messages 리스트에 추가
                //addChatItem(text, MessageType.USER_INPUT)
                // 추가: messages 리스트의 내용을 로그나 UI에 표시하려면 여기에 코드를 추가하세요.
                // 예를 들어, 로그를 사용하여 추가된 메시지를 확인할 수 있습니다.
                Log.d("searchFragment", "인식된 메시지: $text")
                // 혹은 인식된 메시지를 UI에 표시하는 등의 작업을 수행할 수도 있습니다.
            }
        }

        // 부분 인식 결과를 사용할 수 있을 때 호출
        override fun onPartialResults(partialResults: Bundle) {}

        // 향후 이벤트를 추가하기 위해 예약
        override fun onEvent(eventType: Int, params: Bundle) {}
    }

    enum class PlayingState {
        PAUSED, PLAYING, STOPPED
    }

    private val errorCallback = { throwable: Throwable -> logError(throwable) }

    private val playerStateEventCallback = Subscription.EventCallback<PlayerState> { playerState ->/*
        Log.v(TAG, String.format("Player State: %s", gson.toJson(playerState)))
        Log.d("playfragment", "update success")*/

        updateTrackCoverArt(playerState)
    }

    private fun updateTrackCoverArt(playerState: PlayerState) {
        assertAppRemoteConnected()
            .imagesApi
            .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
            .setResultCallback { bitmap ->
                binding.btnMusic.background = BitmapDrawable(resources, bitmap)
            }
    }

    // ViewModel로부터 추천 음악 리스트를 업데이트 받는 코드
    private fun observeViewModel() {
        playViewModel.recommendMusicList.observe(this) { newMusicList ->
            if (newMusicList.lists.isNotEmpty()) {
                recommendedMusicList.addAll(newMusicList.lists)
                connectToSpotify()
            } else {
                Log.d("naviActivity", "No music lists received yet.")
                closeLoading()
            }
        }
    }

    private fun getRecommendMusic(situation:String){
        showLoading()
        playViewModel.getRecommendMusic(currentLatitude.toString(), currentLongitude.toString(), situation)
    }

    override fun onStop() {
        super.onStop()
//        animator.cancel() // 애니메이션 중지
        SpotifyAppRemote.disconnect(spotifyAppRemote)
        onDisconnected()
    }

    // Api 호출이 시작되면 LoadingDialogFragment를 보여준다.
    private fun showLoading() {
      Toast.makeText(
          this,
          R.string.music_recommend_loading,
          Toast.LENGTH_LONG
      ).show()
    }

    private fun closeLoading() {
        Toast.makeText(
            this,
            R.string.music_recommend_success,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun onConnected() {
        Log.d("naviActivity", "onconnected 실행!")

        onSubscribedToPlayerStateButtonClicked()
        onSubscribedToPlayerContextButtonClicked()

        //Spotify에 연결되었을 때 uri 실행
        playUri()
        playViewModel.loginSpotify()
    }

    private fun onDisconnected() {
    }

    private fun connectToSpotify() {
        Log.d("naviActivity", "Attempting to connect to Spotify...")
        SpotifyAppRemote.setDebugMode(true)
        connect(false)
    }

    private fun connect(showAuthView: Boolean) {
        //SpotifyAppRemote.disconnect(spotifyAppRemote)
        Log.d("mainActivity", "spotify connect method")
        lifecycleScope.launch {
            try {
                Log.d("mainActivity", "connect to app remote start")
                spotifyAppRemote = connectToAppRemote()
                onConnected()
            } catch (error: Throwable) {
                logError(error)
            }
            if (SpotifyRemoteManager.spotifyAppRemote == null) {
                Log.e("mainActivity", "spotifyAppRemote is null")
            } else {
                Log.e("mainActivity", "spotify app remote is not null")
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
                        Log.d("naviActivity", "SpotifyAppRemote connected")
                        cont.resume(spotifyAppRemote)
                    }

                    override fun onFailure(error: Throwable) {
                        Log.e("naviActivity", "Failed to connect to SpotifyAppRemote", error)
                        cont.resumeWithException(error)
                    }
                })
        }

    private fun playUri() {
        if (spotifyAppRemote == null || !spotifyAppRemote!!.isConnected) {
            Log.e("naviActivity", "SpotifyAppRemote is not connected, cannot play URI")
            return
        }

        Log.d("naviActivity","playUri - recommendMusicList: ${recommendedMusicList}")
        val uri="spotify:track:${recommendedMusicList[currentTrackIndex].trackId}"
        assertAppRemoteConnected()
            .playerApi
            .play(uri)
            .setResultCallback { logMessage(getString(R.string.command_feedback, "play")) }
            .setErrorCallback(errorCallback)
        //playViewModel.getFavoriteMusicList()
        Log.d("naviActivity","음악 재생 시작")
    }

    fun onSubscribedToPlayerContextButtonClicked() {
        playerContextSubscription = cancelAndResetSubscription(playerContextSubscription)

        //binding.currentContextLabel.visibility = View.VISIBLE
        playerContextSubscription = assertAppRemoteConnected()
            .playerApi
            .subscribeToPlayerContext()
            //.setEventCallback(playerContextEventCallback)
            .setErrorCallback { throwable ->
                //binding.currentContextLabel.visibility = View.INVISIBLE
                logError(throwable)
            } as Subscription<PlayerContext>
    }


    private fun onSubscribedToPlayerStateButtonClicked() {
        playerStateSubscription = cancelAndResetSubscription(playerStateSubscription)


        playerStateSubscription = assertAppRemoteConnected()
            .playerApi
            .subscribeToPlayerState()
            .setEventCallback(playerStateEventCallback)
            .setLifecycleCallback(
                object : Subscription.LifecycleCallback {
                    override fun onStart() {
                        logMessage("Event: start")
                        Log.d("naviActivity", "노래 시작!")
                    }

                    override fun onStop() {
                        logMessage("Event: end")
                    }
                })
            .setErrorCallback {
            } as Subscription<PlayerState>

        //playViewModel.getFavoriteMusicList()
    }

    private fun <T : Any?> cancelAndResetSubscription(subscription: Subscription<T>?): Subscription<T>? {
        return subscription?.let {
            if (!it.isCanceled) {
                it.cancel()
            }
            null
        }
    }

    private fun assertAppRemoteConnected(): SpotifyAppRemote {
        spotifyAppRemote?.let {
            if (it.isConnected) {
                return it
            }
        }
        Log.e(NaviActivity.TAG, getString(R.string.err_spotify_disconnected))
        throw SpotifyDisconnectedException()
    }

    private fun logError(throwable: Throwable) {
        //Toast.makeText(requireContext(), R.string.err_generic_toast, Toast.LENGTH_SHORT).show()
        Log.e(NaviActivity.TAG, "", throwable)
    }

    private fun logMessage(msg: String, duration: Int = Toast.LENGTH_SHORT) {
        //Toast.makeText(requireContext(), msg, duration).show()
        //Log.d(TAG, msg)
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
    override fun guidanceRouteUnchangedWithError(
        aGuidnace: KNGuidance,
        aError: KNError
    ) {
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
        Log.d("naviActivity","=========안내 종료========")
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
        knNaviView.guidanceDidUpdateLocation(aGuidance, aLocationGuide)
    }

    // KNGuidance_RouteGuideDelegate

    // 경로 안내 정보 업데이트 시 호출. `routeGuide`의 항목이 1개 이상 변경 시 전달됨.
    override fun guidanceDidUpdateRouteGuide(
        aGuidance: KNGuidance,
        aRouteGuide: KNGuide_Route
    ) {
        knNaviView.guidanceDidUpdateRouteGuide(aGuidance, aRouteGuide)
    }

    // KNGuidance_SafetyGuideDelegate

    // 안전 운행 정보 업데이트 시 호출. `safetyGuide`의 항목이 1개 이상 변경 시 전달됨.
    override fun guidanceDidUpdateSafetyGuide(
        aGuidance: KNGuidance,
        aSafetyGuide: KNGuide_Safety?
    ) {
        knNaviView.guidanceDidUpdateSafetyGuide(aGuidance, aSafetyGuide)
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
    override fun willPlayVoiceGuide(
        aGuidance: KNGuidance,
        aVoiceGuide: KNGuide_Voice
    ) {
        knNaviView.willPlayVoiceGuide(aGuidance, aVoiceGuide)
    }

    // 음성 안내 종료
    override fun didFinishPlayVoiceGuide(
        aGuidance: KNGuidance,
        aVoiceGuide: KNGuide_Voice
    ) {
        knNaviView.didFinishPlayVoiceGuide(aGuidance, aVoiceGuide)
    }

    // KNGuidance_CitsGuideDelegate

    // CITS 정보 변경 시 호출
    override fun didUpdateCitsGuide(
        aGuidance: KNGuidance,
        aCitsGuide: KNGuide_Cits
    ) {
        knNaviView.didUpdateCitsGuide(aGuidance, aCitsGuide)
    }

    override fun naviViewGuideEnded() {
        finish()
        Log.d("naviActivity", "안내 종료 버튼 클릭")
    }

    override fun naviViewGuideState(state: KNGuideState) {
        // 여기에 메서드가 호출될 때 수행할 작업을 구현하세요.
        Log.d("naviActivity", "Guide state changed: $state")
    }

    override fun onDestroy() {
        super.onDestroy()
        naviView?.let {
            it.guideStateDelegate = null
            it.locationGuideDelegate = null
            it.routeGuideDelegate = null
            it.safetyGuideDelegate = null
            it.voiceGuideDelegate = null
            it.citsGuideDelegate = null

            // 경로 초기화
            it.cancelRoute()
            KNSDK.KNSDKRelease()
        }
    }
}