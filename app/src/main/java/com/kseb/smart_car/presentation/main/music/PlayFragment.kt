package com.kseb.smart_car.presentation.main.music

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.GsonBuilder
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.FragmentPlayBinding
import com.kseb.smart_car.extension.GetFavoriteMusicState
import com.kseb.smart_car.presentation.main.MainViewModel
import com.kseb.smart_car.presentation.main.map.navi.LoadingDialogFragment
import com.kseb.smart_car.presentation.main.music.PlayFragment.AuthParams.CLIENT_ID
import com.kseb.smart_car.presentation.main.music.PlayFragment.AuthParams.REDIRECT_URI
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

class PlayFragment : Fragment() {
    private var _binding: FragmentPlayBinding? = null
    private val binding: FragmentPlayBinding
        get() = requireNotNull(_binding) { "null" }

    private val playViewModel:PlayViewModel by activityViewModels()
    private val mainViewModel:MainViewModel by activityViewModels()
    private var loadingDialog:LoadingDialogFragment?=null

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
    }

    private val gson = GsonBuilder().setPrettyPrinting().create()

    private var playerStateSubscription: Subscription<PlayerState>? = null
    private var playerContextSubscription: Subscription<PlayerContext>? = null

    private lateinit var views: List<View>
    private lateinit var trackProgressBar: TrackProgressBar

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
        }

        binding.btnCurrentTrackSinger.apply {
            text = String.format(Locale.US, "%s", playerState.track.artist.name)
            Log.d("playfragment", "label: ${text}")
            tag = playerState
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

                                Log.d("ivMusic Info", "Width: $width, Height: $height")
                                Log.d("ivMusic Info", "Position - X: $x, Y: $y")

                                // Check if ivMusic has a valid size
                                if (binding.ivMusic.width > 0 && binding.ivMusic.height > 0) {
                                    val constraintSet = ConstraintSet()
                                    constraintSet.clone(binding.root as ConstraintLayout)

                                    Log.d("playfragment", "radius_music_shadow 적용 시작")

                                    // Get the shadow size from dimens
                                    val shadowSize = resources.getDimensionPixelSize(R.dimen.radius_music_shadow)

                                    // Set the shadow size (make it larger than ivMusic)
                                    val shadowWidth = (binding.ivMusic.width * 1.2).toInt()
                                    val shadowHeight = (binding.ivMusic.height * 1.2).toInt()

                                    constraintSet.constrainWidth(R.id.iv_shadow, shadowWidth)
                                    constraintSet.constrainHeight(R.id.iv_shadow, shadowHeight)

                                    // Center the shadow around ivMusic
                                    constraintSet.connect(R.id.iv_shadow, ConstraintSet.TOP, R.id.iv_music, ConstraintSet.TOP)
                                    constraintSet.connect(R.id.iv_shadow, ConstraintSet.START, R.id.iv_music, ConstraintSet.START)
                                    constraintSet.connect(R.id.iv_shadow, ConstraintSet.END, R.id.iv_music, ConstraintSet.END)
                                    constraintSet.connect(R.id.iv_shadow, ConstraintSet.BOTTOM, R.id.iv_music, ConstraintSet.BOTTOM)

                                    constraintSet.applyTo(binding.root as ConstraintLayout)

                                    Log.d("playfragment", "위치 선정 끝")

                                    // Verify layout update
                                    binding.ivShadow.post {
                                        val width = binding.ivShadow.width
                                        val height = binding.ivShadow.height

                                        val location = IntArray(2)
                                        binding.ivShadow.getLocationOnScreen(location)
                                        val x = location[0]
                                        val y = location[1]

                                        Log.d("ivShadow Info", "Width: $width, Height: $height")
                                        Log.d("ivShadow Info", "Position - X: $x, Y: $y")
                                    }

                                    // Log ivMusic's final size and position
                                    val musicWidth = binding.ivMusic.width
                                    val musicHeight = binding.ivMusic.height

                                    val musicLocation = IntArray(2)
                                    binding.ivMusic.getLocationOnScreen(musicLocation)
                                    val musicX = musicLocation[0]
                                    val musicY = musicLocation[1]

                                    Log.d("ivMusic Info", "Width: $musicWidth, Height: $musicHeight")
                                    Log.d("ivMusic Info", "Position - X: $musicX, Y: $musicY")
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

        if(spotifyAppRemote!=null){
            Log.d("playfragment","spotify app remote is not null")
        }else{
            Log.d("playfragment","spotify app remote is null")
        }
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

        getAccessToken()
        SpotifyAppRemote.setDebugMode(true)
        clickButton()

        connectToSpotify()
    }

    private fun getAccessToken(){
        mainViewModel.accessToken.observe(viewLifecycleOwner){token->
            playViewModel.setAccessToken(token)
        }
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
        loadingDialog = LoadingDialogFragment()
        loadingDialog?.show(parentFragmentManager, "LoadingDialog")
    }

    // 데이터 로딩이 완료 되면 LoadingDialogFragment를 dismiss 한다.
    private fun closeLoadingActivity() {
        loadingDialog?.dismiss()
    }

    private fun onConnected() {
        for (input in views) {
            input.isEnabled = true
        }

        onSubscribedToPlayerStateButtonClicked()
        onSubscribedToPlayerContextButtonClicked()

        //Spotify에 연결되었을 때 uri 실행
        playUri(SpotifySampleContexts.TRACK_URI)
        playViewModel.loginSpotify()
    }

    private fun onDisconnected() {
        for (view in views) {
            view.isEnabled = false
        }
        binding.ivMusic.setImageResource(R.drawable.widget_placeholder)
    }

    private fun connectToSpotify() {
        connect(false)
    }

    private fun connect(showAuthView: Boolean) {
        //SpotifyAppRemote.disconnect(spotifyAppRemote)
        lifecycleScope.launch {
            try {
                spotifyAppRemote = connectToAppRemote(showAuthView)
                onConnected()
            } catch (error: Throwable) {
                onDisconnected()
                logError(error)
            }
        }
    }

    private suspend fun connectToAppRemote(showAuthView: Boolean): SpotifyAppRemote? =
        suspendCoroutine { cont: Continuation<SpotifyAppRemote> ->
            SpotifyAppRemote.connect(
                requireActivity().application,
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
                        cont.resumeWithException(error)
                    }
                })
        }

    private fun playUri(uri: String) {
        assertAppRemoteConnected()
            .playerApi
            .play(uri)
            .setResultCallback { logMessage(getString(R.string.command_feedback, "play")) }
            .setErrorCallback(errorCallback)
        //playViewModel.getFavoriteMusicList()
        Log.d("playFragment","음악 재생 시작")
    }

    private fun onSkipPreviousButtonClicked(notUsed: View) {
        assertAppRemoteConnected()
            .playerApi
            .skipPrevious()
            .setResultCallback { logMessage(getString(R.string.command_feedback, "skip previous")) }
            .setErrorCallback(errorCallback)
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
        assertAppRemoteConnected()
            .playerApi
            .skipNext()
            .setResultCallback { logMessage(getString(R.string.command_feedback, "skip next")) }
            .setErrorCallback(errorCallback)
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

    fun setSpotifyAppRemote(remote: SpotifyAppRemote?) {
        this.spotifyAppRemote = remote
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}