package com.kseb.smart_car.presentation.main.music

import android.Manifest
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.GsonBuilder
import com.kseb.smart_car.R
import com.kseb.smart_car.data.responseDto.ResponseRecommendMusicDto
import com.kseb.smart_car.databinding.FragmentPlayBinding
import com.kseb.smart_car.extension.GetFavoriteMusicState
import com.kseb.smart_car.presentation.main.MainViewModel
import com.kseb.smart_car.presentation.main.map.navi.LoadingDialogFragment
import com.kseb.smart_car.presentation.main.music.PlayFragment.AuthParams.CLIENT_ID
import com.kseb.smart_car.presentation.main.music.PlayFragment.AuthParams.REDIRECT_URI
import com.kseb.smart_car.presentation.main.music.PlayFragment.SpotifySampleContexts.TRACK_URI
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.SpotifyDisconnectedException
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.Image
import com.spotify.protocol.types.PlayerContext
import com.spotify.protocol.types.PlayerState
import com.spotify.sdk.demo.TrackProgressBar
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.properties.Delegates

class PlayFragment : Fragment() {
    private var _binding: FragmentPlayBinding? = null
    private val binding: FragmentPlayBinding
        get() = requireNotNull(_binding) { "null" }

    private val playViewModel:PlayViewModel by activityViewModels()
    private val mainViewModel:MainViewModel by activityViewModels()
    private var loadingDialog:LoadingDialogFragment?=null

    private var hasCheckedSpotifyAppRemote = false
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private lateinit var playAdapter: PlayAdapter
    private lateinit var animator: ObjectAnimator

    object AuthParams {
        const val CLIENT_ID = "496b4681f0784ab6a7b1433d22b12b92"
        const val REDIRECT_URI = "https://com.kseb.smart_car/callback"
    }

    object SpotifySampleContexts {
        const val TRACK_URI = "spotify:track:5sdQOyqq2IDhvmx2lHOpwd"
    }

    companion object {
        const val TAG = "Spotify"
        const val STEP_MS = 15000L

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private val gson = GsonBuilder().setPrettyPrinting().create()

    private var playerStateSubscription: Subscription<PlayerState>? = null
    private var playerContextSubscription: Subscription<PlayerContext>? = null

    private lateinit var views: List<View>
    private lateinit var trackProgressBar: TrackProgressBar

    private lateinit var situation: String
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude by Delegates.notNull<Double>()
    private var longitude by Delegates.notNull<Double>()
    private val REQUEST_CODE_LOCATION_PERMISSION = 123

    // 현재 재생 중인 곡의 인덱스
    private var currentTrackIndex = 0
    // 추천 음악 리스트
    private val recommendedMusicList = mutableListOf<ResponseRecommendMusicDto.RecommendMusicList>()

    enum class PlayingState {
        PAUSED, PLAYING, STOPPED
    }

    private val errorCallback = { throwable: Throwable -> logError(throwable) }

    private val playerContextEventCallback =
        Subscription.EventCallback<PlayerContext> { playerContext ->
            binding.btnCurrentTrackLabel.apply {
                text =
                    String.format(Locale.US, "%s\n%s", playerContext.title, playerContext.subtitle)
                tag = playerContext
            }
        }

    private val playerStateEventCallback = Subscription.EventCallback<PlayerState> { playerState ->/*
        Log.v(TAG, String.format("Player State: %s", gson.toJson(playerState)))
        Log.d("playfragment", "update success")*/

        updateTrackStateButton(playerState)

        updatePlayPauseButton(playerState)

        updateTrackCoverArt(playerState)

        updateSeekbar(playerState)

        updateFavoriteButton(playerState)

        updateUnfavoriteButton(playerState)
    }

    private fun updatePlayPauseButton(playerState: PlayerState) {
        // Invalidate play / pause
        if (playerState.isPaused) {
            binding.btnPlayPauseButton.setImageResource(R.drawable.btn_play)
        } else {
            binding.btnPlayPauseButton.setImageResource(R.drawable.btn_pause)
        }
    }

    private fun updateTrackStateButton(playerState: PlayerState) {
        binding.btnCurrentTrackLabel.apply {
            text = String.format(Locale.US, "%s", playerState.track.name)
            Log.d("playfragment", "label: ${text}")
            tag = playerState
            isSelected = true // 이 속성이 설정되어야 마키 효과가 작동함
            ellipsize = TextUtils.TruncateAt.MARQUEE
            marqueeRepeatLimit = -1 // 무한 반복
            isSingleLine = true // 한 줄로 제한
        }

        binding.btnCurrentTrackSinger.apply {
            text = String.format(Locale.US, "%s", playerState.track.artist.name)
            Log.d("playfragment", "label: ${text}")
            tag = playerState
            isSelected = true // 이 속성이 설정되어야 마키 효과가 작동함
            ellipsize = TextUtils.TruncateAt.MARQUEE
            marqueeRepeatLimit = -1 // 무한 반복
            isSingleLine = true // 한 줄로 제한
        }
    }

    private fun updateSeekbar(playerState: PlayerState) {
        // Update progressbar
        trackProgressBar.apply {
            if (playerState.playbackSpeed > 0) {
                unpause()
            } else {
                pause()
            }
            // Invalidate seekbar length and position
            binding.sbBar.max = playerState.track.duration.toInt()
            binding.sbBar.isEnabled = true
            setDuration(playerState.track.duration)
            update(playerState.playbackPosition)
        }
    }

    private fun updateTrackCoverArt(playerState: PlayerState) {
        assertAppRemoteConnected()
            .imagesApi
            .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
            .setResultCallback { bitmap ->
                binding.ivMusic.load(bitmap) {
                    crossfade(true)
                    transformations(RoundedCornersTransformation(requireContext().resources.getDimension(R.dimen.radius_music_image)))
                    listener(
                        onSuccess = { _, _ ->
                            // 이미지 로드 성공 후의 작업
                            binding.ivMusic.post {
                                val width = binding.ivMusic.width
                                val height = binding.ivMusic.height
                                val location = IntArray(2)
                                binding.ivMusic.getLocationOnScreen(location)
                                val x = location[0]
                                val y = location[1]

                                // ivMusic 사이즈 체크
                                if (binding.ivMusic.width > 0 && binding.ivMusic.height > 0) {
                                    val constraintSet = ConstraintSet()
                                    constraintSet.clone(binding.root as ConstraintLayout)

                                    // ivShadow 크기 설정
                                    val shadowSize = resources.getDimensionPixelSize(R.dimen.radius_music_shadow)
                                    val shadowWidth = (binding.ivMusic.width * 1.2).toInt()
                                    val shadowHeight = (binding.ivMusic.height * 1.2).toInt()

                                    constraintSet.constrainWidth(R.id.iv_shadow, shadowWidth)
                                    constraintSet.constrainHeight(R.id.iv_shadow, shadowHeight)

                                    // ivShadow 위치 설정
                                    constraintSet.connect(R.id.iv_shadow, ConstraintSet.TOP, R.id.iv_music, ConstraintSet.TOP)
                                    constraintSet.connect(R.id.iv_shadow, ConstraintSet.START, R.id.iv_music, ConstraintSet.START)
                                    constraintSet.connect(R.id.iv_shadow, ConstraintSet.END, R.id.iv_music, ConstraintSet.END)
                                    constraintSet.connect(R.id.iv_shadow, ConstraintSet.BOTTOM, R.id.iv_music, ConstraintSet.BOTTOM)

                                    constraintSet.applyTo(binding.root as ConstraintLayout)

                                    Log.d("playfragment", "위치 선정 끝")
                                } else {
                                    Log.d("playfragment", "ivMusic 크기나 위치가 아직 결정되지 않았습니다.")
                                }

                            }
                        },
                        onError = { _, _ ->
                            Log.d("ivMusic Info", "이미지 로드 실패")
                        }
                    )
                }

                // ivMusic에 기존 이미지를 로드 (원형 아님)
                /*binding.ivMusic.load(bitmap) {
                    transformations(RoundedCornersTransformation(requireContext().resources.getDimensionPixelSize(
                        R.dimen.radius_music_image
                    ).toFloat()))
                }*/

                // ivAlbum에 원형으로 변환하여 이미지 로드
                binding.ivAlbum.load(bitmap) {
                    transformations(CircleCropTransformation()) // 이미지 원형 변환
                }
               /* binding.ivMusic.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        binding.ivMusic.viewTreeObserver.removeOnGlobalLayoutListener(this)

                        val constraintSet = ConstraintSet()
                        constraintSet.clone(binding.root as ConstraintLayout)

                        // 크기를 1.2배로 설정
                        constraintSet.constrainWidth(R.id.iv_shadow, (binding.ivMusic.width * 1.2).toInt())
                        constraintSet.constrainHeight(R.id.iv_shadow, (binding.ivMusic.height * 1.2).toInt())

                        constraintSet.connect(R.id.iv_shadow, ConstraintSet.TOP, R.id.iv_music, ConstraintSet.TOP)
                        constraintSet.connect(R.id.iv_shadow, ConstraintSet.BOTTOM, R.id.iv_music, ConstraintSet.BOTTOM)
                        constraintSet.connect(R.id.iv_shadow, ConstraintSet.START, R.id.iv_music, ConstraintSet.START)
                        constraintSet.connect(R.id.iv_shadow, ConstraintSet.END, R.id.iv_music, ConstraintSet.END)

                        constraintSet.setDimensionRatio(R.id.iv_shadow, "W,1:1")

                        constraintSet.applyTo(binding.root as ConstraintLayout)
                        //Log.d("PlayFragment", "ivShadow width: ${binding.ivShadow.width}, height: ${binding.ivShadow.height}")
                    }
                })*/

                // 회전 애니메이션 초기화
                if (!this::animator.isInitialized) {
                    animator = ObjectAnimator.ofFloat(binding.ivMusic, View.ROTATION, 0f, 360f).apply {
                        duration = 4000 // 애니메이션 지속 시간
                        repeatCount = ObjectAnimator.INFINITE // 무한 반복
                        interpolator = LinearInterpolator() // 일정한 속도로 회전
                    }
                }

                if (playerState.isPaused) {
                    // 애니메이션 멈춤
                    animator.cancel()
                } else {
                    // 애니메이션 시작
                    animator.start()
                }
            }
    }

    private fun updateNextMusicList(playerState: PlayerState){

    }

    private fun updateFavoriteButton(playerState: PlayerState){
        playViewModel.getFavoriteMusicList()
        Log.d("playFragment","playerState: ${playerState}")
        lifecycleScope.launch {
            playViewModel.favoriteMusicState.collect{favoriteMusicState ->
                when(favoriteMusicState){
                    is GetFavoriteMusicState.Success -> {
                        if (favoriteMusicState.favoriteMusicDto.favoritesMusicList.any { it.trackId == playerState.track.uri }) {
                            binding.btnFavorite.isSelected = true
                        } else {
                            binding.btnFavorite.isSelected = false
                        }
                    }
                    is GetFavoriteMusicState.Loading->{}
                    is GetFavoriteMusicState.Error->{
                        Log.e("playFragment","update favorite button error!: ${favoriteMusicState.message}")
                    }
                }
            }
        }

        clickFavorite(playerState)
    }

    private fun updateUnfavoriteButton(playerState: PlayerState){
        binding.btnUnfavorite.setOnClickListener {
            binding.btnUnfavorite.isSelected = !binding.btnUnfavorite.isSelected
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("playfragment", "onViewCreated 호출됨")
        // 전달된 데이터 받기
        arguments?.getString("situation_key")?.let {
            situation = it
            // 상황에 맞게 처리
            Log.d("PlayFragment", "Received situation: $situation")
        }
        //initializeSpotifyConnection()

        binding.sbBar.apply {
            isEnabled = false
            progressDrawable.setColorFilter(resources.getColor(R.color.bnv_clicked_black), PorterDuff.Mode.SRC_ATOP)
            indeterminateDrawable.setColorFilter(resources.getColor(R.color.bnv_clicked_black), PorterDuff.Mode.SRC_ATOP)
        }

        trackProgressBar =
            TrackProgressBar(requireContext(), binding.sbBar) { seekToPosition: Long -> seekTo(seekToPosition) }

        views = listOf(
            binding.btnPlayPauseButton,
            binding.btnSkipPrevButton,
            binding.btnSkipNextButton,
            binding.sbBar
        )

        //다음곡 리스트
       /* lifecycleScope.launch {
            playViewModel.isLoginSpotify.observe(viewLifecycleOwner){
                if(it){
                    playAdapter= spotifyAppRemote?.let { PlayAdapter(requireContext(), it) }!!
                    playAdapter.getList(playViewModel.nextMusicList)
                }

            }
        }*/
        showLoadingActivity()
        getAccessToken()
        getLastKnownLocation()
        SpotifyAppRemote.setDebugMode(true)
        clickButton()

        // 뒤로가기 버튼을 처리하는 콜백을 추가합니다.
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // ViewModel의 recommendMusicList를 초기화합니다.
                playViewModel.recommendMusicListReset()

                // 기본 뒤로가기 동작을 수행하도록 합니다.
                // 이 콜백이 활성화되어 있지 않으면 시스템의 기본 동작이 수행됩니다.
                if (isEnabled) {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        })
        //connectToSpotify()
    }

    private fun getAccessToken(){
        mainViewModel.accessToken.observe(viewLifecycleOwner){token->
            playViewModel.setAccessToken(token)
            observeViewModel()
        }
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latitude=location.latitude
                    longitude=location.longitude
                    getRecommendMusic(latitude.toString(), longitude.toString())
                    return@addOnSuccessListener
                }
            }
    }

    // ViewModel로부터 추천 음악 리스트를 업데이트 받는 코드
    private fun observeViewModel() {
        playViewModel.recommendMusicList.observe(viewLifecycleOwner) { newMusicList ->
            if (newMusicList.lists.isNotEmpty()) {
                recommendedMusicList.addAll(newMusicList.lists)
                Log.d("playFragment", "observeViewModel - list: ${newMusicList.lists}\nrecommendlist: ${recommendedMusicList}")

                // 초기 연결 상태만 확인하고 이후에는 별도로 관리
                if (!hasCheckedSpotifyAppRemote) {
                    playViewModel.spotifyAppRemote.observe(viewLifecycleOwner) { remote ->
                        spotifyAppRemote = remote
                        hasCheckedSpotifyAppRemote = true // 연결 상태 확인 완료
                        Log.d("playfragment", "observeviewmodel - remote: ${spotifyAppRemote}")

                        lifecycleScope.launch {
                            if (spotifyAppRemote != null && spotifyAppRemote!!.isConnected) {
                                Log.d("playfragment", "observeViewModel - 이미 연결됨!")
                                onConnected()
                                playUri() // 연결된 후에만 playUri를 호출
                            } else {
                                Log.d("playfragment", "observeViewModel - 연결 시도")
                                connectToSpotify().also {
                                    playUri() // 연결된 후에만 playUri를 호출
                                }
                            }
                            closeLoadingActivity()
                        }
                    }
                } else {
                    lifecycleScope.launch {
                        // Spotify 연결 상태가 이미 확인된 경우, 바로 플레이 시도
                        if (spotifyAppRemote != null && spotifyAppRemote!!.isConnected) {
                            playUri()
                        } else {
                            Log.e("playFragment", "SpotifyAppRemote is not connected, cannot play URI")
                        }
                        closeLoadingActivity()
                    }
                }
            } else {
                Log.d("playFragment", "No music lists received yet.")
            }
        }
    }


    private fun getRecommendMusic(latitude:String, longitude:String){
        playViewModel.getRecommendMusic(latitude,longitude,situation)
    }

    private fun seekTo(seekToPosition: Long) {
        assertAppRemoteConnected()
            .playerApi
            .seekTo(seekToPosition)
            .setErrorCallback(errorCallback)
    }

    override fun onStop() {
        super.onStop()
//        animator.cancel() // 애니메이션 중지
        SpotifyAppRemote.disconnect(spotifyAppRemote)
        onDisconnected()
    }

    // Api 호출이 시작되면 LoadingDialogFragment를 보여준다.
    private fun showLoadingActivity() {
        loadingDialog = LoadingDialogFragment("play")
        loadingDialog?.show(parentFragmentManager, "LoadingDialog")
    }

    // 데이터 로딩이 완료 되면 LoadingDialogFragment를 dismiss 한다.
    private fun closeLoadingActivity() {
        loadingDialog?.dismiss()
    }

    private fun onConnected() {
        Log.d("playfragment", "onconnected 실행!")
        for (input in views) {
            input.isEnabled = true
        }

        onSubscribedToPlayerStateButtonClicked()
        onSubscribedToPlayerContextButtonClicked()

        //Spotify에 연결되었을 때 uri 실행
        //playUri(SpotifySampleContexts.TRACK_URI)
        playViewModel.loginSpotify()
    }

    private fun onDisconnected() {
        for (view in views) {
            view.isEnabled = false
        }
        binding.ivMusic.setImageResource(R.drawable.widget_placeholder)
    }

    private suspend fun connectToSpotify() {
        if (spotifyAppRemote == null || !spotifyAppRemote!!.isConnected) {
            spotifyAppRemote = connectToAppRemote()
            if (spotifyAppRemote != null) {
                Log.d("playfragment", "SpotifyAppRemote connected in connectToSpotify")
                onConnected()
            } else {
                Log.e("playfragment", "Failed to connect SpotifyAppRemote in connectToSpotify")
            }
        } else {
            Log.d("playfragment", "SpotifyAppRemote already connected")
        }
    }

   /* private fun connect(showAuthView: Boolean) {
        //SpotifyAppRemote.disconnect(spotifyAppRemote)
        lifecycleScope.launch {
            try {
                if (spotifyAppRemote == null || !spotifyAppRemote!!.isConnected) {
                    spotifyAppRemote = connectToAppRemote()
                    onConnected()
                    Log.d("playfragment","connect - remote 연결 됐고 onconnected함")
                } else {
                    onConnected()
                    Log.d("playfragment","connect - remote 연결 안됐고 onconnected함")
                }
            } catch (error: Throwable) {
                logError(error)
                onDisconnected()
            }
        }
    }*/

    private suspend fun connectToAppRemote(): SpotifyAppRemote? =
        suspendCoroutine { cont: Continuation<SpotifyAppRemote> ->
            SpotifyAppRemote.connect(
                requireActivity().application,
                ConnectionParams.Builder(CLIENT_ID)
                    .setRedirectUri(REDIRECT_URI)
                    .showAuthView(true)
                    .build(),
                object : Connector.ConnectionListener {
                    override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                        Log.d("playfragment", "SpotifyAppRemote connected")
                        cont.resume(spotifyAppRemote)
                    }

                    override fun onFailure(error: Throwable) {
                        Log.e("playfragment", "Failed to connect to SpotifyAppRemote", error)
                        cont.resumeWithException(error)
                    }
                })
        }

    private fun playUri() {
        if (spotifyAppRemote == null || !spotifyAppRemote!!.isConnected) {
            Log.e("playFragment", "SpotifyAppRemote is not connected, cannot play URI")
            return
        }

        Log.d("playFragment","playUri - recommendMusicList: ${recommendedMusicList}")
        //val uri="spotify:track:${recommendedMusicList[currentTrackIndex].trackId}"
        val uri=TRACK_URI
        assertAppRemoteConnected()
            .playerApi
            .play(uri)
            .setResultCallback { logMessage(getString(R.string.command_feedback, "play")) }
            .setErrorCallback(errorCallback)
        //playViewModel.getFavoriteMusicList()
        Log.d("playFragment","음악 재생 시작")
    }

    private fun onSkipPreviousButtonClicked(notUsed: View) {
        if(currentTrackIndex==0){
            playUri()
        }else{
            currentTrackIndex--
            playUri()
        }
    }

    private fun onPlayPauseButtonClicked(notUsed: View) {
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
                    }
                }
        }
    }

    private fun onSkipNextButtonClicked(notUsed: View) {
        lifecycleScope.launch {
            getLastKnownLocation()
            currentTrackIndex++
            playUri()
        }
        /*assertAppRemoteConnected()
            .playerApi
            .skipNext()
            .setResultCallback { logMessage(getString(R.string.command_feedback, "skip next")) }
            .setErrorCallback(errorCallback)*/
    }

    fun onSubscribedToPlayerContextButtonClicked() {
        playerContextSubscription = cancelAndResetSubscription(playerContextSubscription)

        //binding.currentContextLabel.visibility = View.VISIBLE
        playerContextSubscription = assertAppRemoteConnected()
            .playerApi
            .subscribeToPlayerContext()
            .setEventCallback(playerContextEventCallback)
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
                        Log.d("playfragment", "노래 시작!")
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
        Log.e(TAG, getString(R.string.err_spotify_disconnected))
        throw SpotifyDisconnectedException()
    }

    private fun logError(throwable: Throwable) {
        //Toast.makeText(requireContext(), R.string.err_generic_toast, Toast.LENGTH_SHORT).show()
        Log.e(TAG, "", throwable)
    }

    private fun logMessage(msg: String, duration: Int = Toast.LENGTH_SHORT) {
        //Toast.makeText(requireContext(), msg, duration).show()
        //Log.d(TAG, msg)
    }
    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(requireContext()).setTitle(title).setMessage(message).create().show()
    }

    private fun clickButton() {
        with(binding) {
            btnSkipPrevButton.setOnClickListener {
                onSkipPreviousButtonClicked(it)
            }
            btnPlayPauseButton.setOnClickListener {
                onPlayPauseButtonClicked(it)
            }
            btnSkipNextButton.setOnClickListener {
                onSkipNextButtonClicked(it)
            }
        }
    }

    private fun clickFavorite(playerState: PlayerState){
        binding.btnFavorite.setOnClickListener{
            if(binding.btnFavorite.isSelected){
                playViewModel.deleteFavoriteMusicList(playerState.track.uri)
                binding.btnFavorite.isSelected=false
            }else{
                playViewModel.addFavoriteMusicList(playerState)
                binding.btnFavorite.isSelected=true
            }
        }
    }

    /*private fun initializeSpotifyConnection() {
        if (spotifyAppRemote != null && spotifyAppRemote!!.isConnected) {
            // 이미 SpotifyAppRemote가 연결되어 있는 경우
            Log.d("playfragment", "SpotifyAppRemote is already connected")
            onConnected() // 연결된 상태에서 필요한 초기화 작업 수행
        } else {
            // SpotifyAppRemote가 null이거나 연결되어 있지 않은 경우
            Log.d("playfragment", "SpotifyAppRemote is not connected, attempting to connect...")
            //connectToSpotify() // 연결 시도
        }
    }*/


    fun setSpotifyAppRemote(remote: SpotifyAppRemote?) {
        this.spotifyAppRemote = remote
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}