package com.kseb.smart_car.presentation.main.music

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.gson.GsonBuilder
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.FragmentPlayBinding
import com.kseb.smart_car.presentation.main.music.PlayFragment.AuthParams.CLIENT_ID
import com.kseb.smart_car.presentation.main.music.PlayFragment.AuthParams.REDIRECT_URI
import com.kseb.smart_car.presentation.main.music.PlayFragment.SpotifySampleContexts.ALBUM_URI
import com.kseb.smart_car.presentation.main.music.PlayFragment.SpotifySampleContexts.ARTIST_URI
import com.kseb.smart_car.presentation.main.music.PlayFragment.SpotifySampleContexts.PLAYLIST_URI
import com.kseb.smart_car.presentation.main.music.PlayFragment.SpotifySampleContexts.PODCAST_URI
import com.kseb.smart_car.presentation.main.music.PlayFragment.SpotifySampleContexts.TRACK_URI
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.ContentApi
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.SpotifyDisconnectedException
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.Capabilities
import com.spotify.protocol.types.Image
import com.spotify.protocol.types.ListItem
import com.spotify.protocol.types.ListItems
import com.spotify.protocol.types.PlayerContext
import com.spotify.protocol.types.PlayerState
import com.spotify.sdk.demo.TrackProgressBar
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PlayFragment: Fragment() {
    private var _binding:FragmentPlayBinding?= null
    private val binding:FragmentPlayBinding
        get()= requireNotNull(_binding){"null"}

    object AuthParams {
        const val CLIENT_ID = "d8e2d4268f28445eac8333a5292c8e9f"
        const val REDIRECT_URI = "https://com.kseb.smart_car/callback"
    }

    object SpotifySampleContexts {
        const val TRACK_URI = "spotify:track:5sdQOyqq2IDhvmx2lHOpwd"
        const val ALBUM_URI = "spotify:album:4m2880jivSbbyEGAKfITCa"
        const val ARTIST_URI = "spotify:artist:3WrFJ7ztbogyGnTHbHJFl2"
        const val PLAYLIST_URI = "spotify:playlist:37i9dQZEVXbMDoHDwVN2tF"
        const val PODCAST_URI = "spotify:show:2tgPYIeGErjk6irHRhk9kj"
    }

    companion object {
        const val TAG = "App-Remote Sample"
        const val STEP_MS = 15000L
    }

    private val gson = GsonBuilder().setPrettyPrinting().create()

    private var playerStateSubscription: Subscription<PlayerState>? = null
    private var playerContextSubscription: Subscription<PlayerContext>? = null
    private var capabilitiesSubscription: Subscription<Capabilities>? = null
    private var spotifyAppRemote: SpotifyAppRemote? = null

    private lateinit var views: List<View>
    private lateinit var trackProgressBar: TrackProgressBar

    enum class PlayingState {
        PAUSED, PLAYING, STOPPED
    }

    private val errorCallback = { throwable: Throwable -> logError(throwable) }

    private val playerContextEventCallback = Subscription.EventCallback<PlayerContext> { playerContext ->
        binding.btnCurrentTrackLabel.apply {
            text = String.format(Locale.US, "%s\n%s", playerContext.title, playerContext.subtitle)
            tag = playerContext
        }
    }

    private val playerStateEventCallback = Subscription.EventCallback<PlayerState> { playerState ->
        Log.v(TAG, String.format("Player State: %s", gson.toJson(playerState)))
        Log.d("playfragment","update success")

        updateTrackStateButton(playerState)

        updatePlayPauseButton(playerState)

        updateTrackCoverArt(playerState)

        updateSeekbar(playerState)
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
            visibility=View.VISIBLE
            text = String.format(Locale.US, "%s\n%s", playerState.track.name, playerState.track.artist.name)
            Log.d("playfragment","label: ${text}")
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
            binding.seekTo.max = playerState.track.duration.toInt()
            binding.seekTo.isEnabled = true
            setDuration(playerState.track.duration)
            update(playerState.playbackPosition)
        }
    }

    private fun updateTrackCoverArt(playerState: PlayerState) {
        // Get image from track
        assertAppRemoteConnected()
            .imagesApi
            .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
            .setResultCallback { bitmap ->
                binding.ivMusic.setImageBitmap(bitmap)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding=FragmentPlayBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.seekTo.apply {
            isEnabled = false
            progressDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
            indeterminateDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        }

        trackProgressBar = TrackProgressBar(binding.seekTo) { seekToPosition: Long -> seekTo(seekToPosition) }

        views = listOf(
            binding.btnPlayPauseButton,
            binding.btnSkipPrevButton,
            binding.btnSkipNextButton,
            binding.connectSwitchToLocal,
            binding.playPodcastButton,
            binding.playTrackButton,
            binding.playAlbumButton,
            binding.playArtistButton,
            binding.playPlaylistButton,
            binding.subscribeToCapabilities,
            binding.getCollectionState,
            binding.removeUri,
            binding.saveUri,
            binding.getFitnessRecommendedItemsButton,
            binding.seekTo)

        SpotifyAppRemote.setDebugMode(true)
        clickButton()

        onDisconnected()
    }

    private fun seekTo(seekToPosition: Long) {
        assertAppRemoteConnected()
            .playerApi
            .seekTo(seekToPosition)
            .setErrorCallback(errorCallback)
    }

    override fun onStop() {
        super.onStop()
        SpotifyAppRemote.disconnect(spotifyAppRemote)
        onDisconnected()
    }

    private fun onConnected() {
        for (input in views) {
            input.isEnabled = true
        }
        binding.btnConnect.apply {
            isEnabled = false
            text = getString(R.string.connected)
        }

        onSubscribedToPlayerStateButtonClicked()
        onSubscribedToPlayerContextButtonClicked()
    }

    private fun onConnecting() {
        binding.btnConnect.apply {
            isEnabled = false
            text = getString(R.string.connecting)
        }
    }

    private fun onDisconnected() {
        for (view in views) {
            view.isEnabled = false
        }

        binding.btnConnect.apply {
            isEnabled = true
            text = getString(R.string.connect)
        }
        binding.ivMusic.setImageResource(R.drawable.widget_placeholder)

        binding.btnCurrentTrackLabel.visibility = View.INVISIBLE
    }

    private fun onConnectClicked(notUsed: View) {
        onConnecting()
        connect(false)
    }

    private fun connect(showAuthView: Boolean) {
        SpotifyAppRemote.disconnect(spotifyAppRemote)
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
                        Log.d("connec","onConnected 실행!")
                        cont.resume(spotifyAppRemote)
                    }

                    override fun onFailure(error: Throwable) {
                        cont.resumeWithException(error)
                    }
                })
        }

    private fun onImageClicked(view: View) {
        assertAppRemoteConnected().let {
            it.playerApi
                .playerState
                .setResultCallback { playerState ->
                    val popupMenu = PopupMenu(requireContext(), view)
                    popupMenu.run {
                        menu.add(720, 720, 0, "Large (720px)")
                        menu.add(480, 480, 1, "Medium (480px)")
                        menu.add(360, 360, 2, "Small (360px)")
                        menu.add(240, 240, 3, "X Small (240px)")
                        menu.add(144, 144, 4, "Thumbnail (144px)")
                        setOnMenuItemClickListener { item ->
                            it.imagesApi
                                .getImage(
                                    playerState.track.imageUri, Image.Dimension.values()[item.order])
                                .setResultCallback { bitmap ->
                                    binding.ivMusic.setImageBitmap(bitmap)
                                }
                            false
                        }
                        show()
                    }
                }
                .setErrorCallback(errorCallback)
        }
    }

    fun onImageScaleTypeClicked(view: View) {
        assertAppRemoteConnected()
            .playerApi
            .playerState
            .setResultCallback {
                val popupMenu = PopupMenu(requireContext(), view)
                popupMenu.run {
                    menu.add(0, ImageView.ScaleType.CENTER.ordinal, 0, "CENTER")
                    menu.add(1, ImageView.ScaleType.CENTER_CROP.ordinal, 1, "CENTER_CROP")
                    menu.add(2, ImageView.ScaleType.CENTER_INSIDE.ordinal, 2, "CENTER_INSIDE")
                    menu.add(3, ImageView.ScaleType.MATRIX.ordinal, 3, "MATRIX")
                    menu.add(4, ImageView.ScaleType.FIT_CENTER.ordinal, 4, "FIT_CENTER")
                    menu.add(4, ImageView.ScaleType.FIT_XY.ordinal, 5, "FIT_XY")
                    setOnMenuItemClickListener { item ->
                        binding.ivMusic.scaleType = ImageView.ScaleType.values()[item.itemId]
                        false
                    }
                    show()
                }

            }
            .setErrorCallback(errorCallback)
    }

    private fun onPlayPodcastButtonClicked(notUsed: View) {
        playUri(PODCAST_URI)
    }

    private fun onPlayTrackButtonClicked(notUsed: View) {
        playUri(TRACK_URI)
    }

    private fun onPlayAlbumButtonClicked(notUsed: View) {
        playUri(ALBUM_URI)
    }

    private fun onPlayArtistButtonClicked(notUsed: View) {
        playUri(ARTIST_URI)
    }

    private fun onPlayPlaylistButtonClicked(notUsed: View) {
        playUri(PLAYLIST_URI)
    }

    private fun playUri(uri: String) {
        assertAppRemoteConnected()
            .playerApi
            .play(uri)
            .setResultCallback { logMessage(getString(R.string.command_feedback, "play")) }
            .setErrorCallback(errorCallback)
    }

    private fun showCurrentPlayerState(view: View) {
        view.tag?.let {
            showDialog("PlayerState", gson.toJson(it))
        }
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
                            .setResultCallback { logMessage(getString(R.string.command_feedback, "play")) }
                            .setErrorCallback(errorCallback)
                    } else {
                        it.playerApi
                            .pause()
                            .setResultCallback { logMessage(getString(R.string.command_feedback, "pause")) }
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

    fun onSeekBack(notUsed: View) {
        assertAppRemoteConnected()
            .playerApi
            .seekToRelativePosition(-STEP_MS)
            .setResultCallback { logMessage(getString(R.string.command_feedback, "seek back")) }
            .setErrorCallback(errorCallback)
    }

    fun onSeekForward(notUsed: View) {
        assertAppRemoteConnected()
            .playerApi
            .seekToRelativePosition(STEP_MS)
            .setResultCallback { logMessage(getString(R.string.command_feedback, "seek fwd")) }
            .setErrorCallback(errorCallback)
    }

    fun onSubscribedToPlayerContextButtonClicked() {
        playerContextSubscription = cancelAndResetSubscription(playerContextSubscription)

        binding.currentContextLabel.visibility = View.VISIBLE
        playerContextSubscription = assertAppRemoteConnected()
            .playerApi
            .subscribeToPlayerContext()
            .setEventCallback(playerContextEventCallback)
            .setErrorCallback { throwable ->
                binding.currentContextLabel.visibility = View.INVISIBLE
                logError(throwable)
            } as Subscription<PlayerContext>
    }

    fun onSubscribeToCapabilitiesClicked(notUsed: View) {
        capabilitiesSubscription = cancelAndResetSubscription(capabilitiesSubscription)

        capabilitiesSubscription = assertAppRemoteConnected()
            .userApi
            .subscribeToCapabilities()
            .setEventCallback { capabilities ->
                logMessage(getString(R.string.on_demand_feedback, capabilities.canPlayOnDemand))
            }
            .setErrorCallback(errorCallback) as Subscription<Capabilities>

        assertAppRemoteConnected()
            .userApi
            .capabilities
            .setResultCallback { capabilities -> logMessage(getString(R.string.on_demand_feedback, capabilities.canPlayOnDemand)) }
            .setErrorCallback(errorCallback)
    }

    private fun onGetCollectionStateClicked(notUsed: View) {
        assertAppRemoteConnected()
            .userApi
            .getLibraryState(TRACK_URI)
            .setResultCallback { libraryState ->
                showDialog(getString(R.string.command_response, getString(R.string.get_collection_state)), gson.toJson(libraryState))
            }
            .setErrorCallback { throwable -> logError(throwable) }
    }

    private fun onRemoveUriClicked(notUsed: View) {
        assertAppRemoteConnected()
            .userApi
            .removeFromLibrary(TRACK_URI)
            .setResultCallback { logMessage(getString(R.string.command_feedback, getString(R.string.remove_uri))) }
            .setErrorCallback { throwable -> logError(throwable) }
    }

    private fun onSaveUriClicked(notUsed: View) {
        assertAppRemoteConnected()
            .userApi
            .addToLibrary(TRACK_URI)
            .setResultCallback { logMessage(getString(R.string.command_feedback, getString(R.string.save_uri))) }
            .setErrorCallback { throwable -> logError(throwable) }
    }

    private fun onGetFitnessRecommendedContentItemsClicked(notUsed: View) {
        assertAppRemoteConnected().let {
            lifecycleScope.launch {
                val combined = ArrayList<ListItem>(50)
                val listItems = loadRootRecommendations(it)
                listItems?.apply {
                    for (i in items.indices) {
                        if (items[i].playable) {
                            combined.add(items[i])
                        } else {
                            val children: ListItems? = loadChildren(it, items[i])
                            combined.addAll(convertToList(children))
                        }
                    }
                }
                showDialog(
                    getString(R.string.command_response, getString(R.string.browse_content)),
                    gson.toJson(combined))
            }
        }
    }

    private fun convertToList(inputItems: ListItems?): List<ListItem> {
        return if (inputItems?.items != null) {
            inputItems.items.toList()
        } else {
            emptyList()
        }
    }

    private suspend fun loadRootRecommendations(appRemote: SpotifyAppRemote): ListItems? =
        suspendCoroutine { cont ->
            appRemote.contentApi
                .getRecommendedContentItems(ContentApi.ContentType.FITNESS)
                .setResultCallback { listItems -> cont.resume(listItems) }
                .setErrorCallback { throwable ->
                    errorCallback.invoke(throwable)
                    cont.resumeWithException(throwable)
                }
        }

    private suspend fun loadChildren(appRemote: SpotifyAppRemote, parent: ListItem): ListItems? =
        suspendCoroutine { cont ->
            appRemote.contentApi
                .getChildrenOfItem(parent, 6, 0)
                .setResultCallback { listItems -> cont.resume(listItems) }
                .setErrorCallback { throwable ->
                    errorCallback.invoke(throwable)
                    cont.resumeWithException(throwable)
                }
        }


    private fun onConnectSwitchToLocalClicked(notUsed: View) {
        assertAppRemoteConnected()
            .connectApi
            .connectSwitchToLocalDevice()
            .setResultCallback { logMessage(getString(R.string.command_feedback, getString(R.string.connect_switch_to_local))) }
            .setErrorCallback(errorCallback)
    }

    private fun onSubscribedToPlayerStateButtonClicked() {
        playerStateSubscription = cancelAndResetSubscription(playerStateSubscription)

        binding.btnCurrentTrackLabel.visibility = View.VISIBLE

        playerStateSubscription = assertAppRemoteConnected()
            .playerApi
            .subscribeToPlayerState()
            .setEventCallback(playerStateEventCallback)
            .setLifecycleCallback(
                object : Subscription.LifecycleCallback {
                    override fun onStart() {
                        logMessage("Event: start")
                        Log.d("playfragment","노래 시작!")
                    }

                    override fun onStop() {
                        logMessage("Event: end")
                    }
                })
            .setErrorCallback {
                binding.btnCurrentTrackLabel.visibility = View.INVISIBLE
            } as Subscription<PlayerState>
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
        Toast.makeText(requireContext(), R.string.err_generic_toast, Toast.LENGTH_SHORT).show()
        Log.e(TAG, "", throwable)
    }

    private fun logMessage(msg: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(requireContext(), msg, duration).show()
        Log.d(TAG, msg)
    }

    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(requireContext()).setTitle(title).setMessage(message).create().show()
    }

    private fun clickButton(){
        with(binding){
            connectSwitchToLocal.setOnClickListener {
                onConnectSwitchToLocalClicked(it)
            }
            playPodcastButton.setOnClickListener {
                onPlayPodcastButtonClicked(it)
            }
            playTrackButton.setOnClickListener {
                onPlayTrackButtonClicked(it)
            }
            playAlbumButton.setOnClickListener {
                onPlayAlbumButtonClicked(it)
            }
            playArtistButton.setOnClickListener {
                onPlayArtistButtonClicked(it)
            }
            playPlaylistButton.setOnClickListener {
                onPlayPlaylistButtonClicked(it)
            }
            getFitnessRecommendedItemsButton.setOnClickListener {
                onGetFitnessRecommendedContentItemsClicked(it)
            }
            subscribeToCapabilities.setOnClickListener {
                onSubscribedToPlayerStateButtonClicked()
            }
            getCollectionState.setOnClickListener {
                onGetCollectionStateClicked(it)
            }
            removeUri.setOnClickListener {
                onRemoveUriClicked(it)
            }
            saveUri.setOnClickListener {
                onSaveUriClicked(it)
            }
            ivMusic.setOnClickListener{
                onImageClicked(it)
            }
            btnCurrentTrackLabel.setOnClickListener {
                showCurrentPlayerState(it)
            }
            btnSkipPrevButton.setOnClickListener{
                onSkipPreviousButtonClicked(it)
            }
            btnPlayPauseButton.setOnClickListener{
                onPlayPauseButtonClicked(it)
            }
            btnSkipNextButton.setOnClickListener{
                onSkipNextButtonClicked(it)
            }
            btnConnect.setOnClickListener{
                onConnectClicked(it)
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }

}