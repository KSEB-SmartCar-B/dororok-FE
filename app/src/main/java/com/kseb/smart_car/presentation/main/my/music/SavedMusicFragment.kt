package com.kseb.smart_car.presentation.main.my.music

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.FragmentSavedmusicBinding
import com.kseb.smart_car.extension.GetFavoriteMusicState
import com.kseb.smart_car.presentation.main.music.PlayFragment
import com.kseb.smart_car.presentation.main.music.PlayFragment.Companion
import com.kseb.smart_car.presentation.main.music.PlayViewModel
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerContext
import com.spotify.protocol.types.PlayerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class SavedMusicFragment : Fragment() {
    private var _binding: FragmentSavedmusicBinding? = null
    private val binding: FragmentSavedmusicBinding
        get() = requireNotNull(_binding) { "null" }

    private lateinit var savedMusicAdapter: SavedMusicAdapter
    private val playViewModel:PlayViewModel by viewModels()
    private val savedMusicViewModel:SavedMusicViewModel by activityViewModels()

    private var spotifyAppRemote: SpotifyAppRemote? = null
    object AuthParams {
        const val CLIENT_ID = "d8e2d4268f28445eac8333a5292c8e9f"
        const val REDIRECT_URI = "https://com.kseb.smart_car/callback"
    }

    companion object {
        const val TAG = "Spotify"
        const val STEP_MS = 15000L
    }

    private val errorCallback = { throwable: Throwable -> logError(throwable) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedmusicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedMusicAdapter = SavedMusicAdapter{ trackId ->
            Log.d("savedMusicFragment","music click!")
            playOrPauseMusic(trackId)
        }
        binding.rvMusic.adapter = savedMusicAdapter
        setAccesstoken()
        clickEditButton()
    }

    private fun setAccesstoken(){
        lifecycleScope.launch {
            savedMusicViewModel.accessToken.observe(viewLifecycleOwner){token->
                playViewModel.setAccessToken(token)
                Log.d("savedMusicFragment","get accessToken")
                getSavedMusic()
            }
        }
    }

    private fun getSavedMusic(){
        playViewModel.getFavoriteMusicList()
        lifecycleScope.launch {
            playViewModel.favoriteMusicState.collect{favoriteMusicState->
                when(favoriteMusicState){
                    is GetFavoriteMusicState.Success->{
                        savedMusicAdapter.setMusicList(favoriteMusicState.favoriteMusicDto.favoritesMusicList)
                    }
                    is GetFavoriteMusicState.Loading->{}
                    is GetFavoriteMusicState.Error->{
                        Log.e("savedMusicFragment","error:${favoriteMusicState.message}")
                    }
                }
            }
        }

    }

    private fun playOrPauseMusic(trackId: String) {
        if (spotifyAppRemote == null) {
            Log.d("savedMusicFragment", "spotifyAppRemote is null")
            return
        }

        spotifyAppRemote?.playerApi?.let { playerApi ->
            playerApi.playerState.setResultCallback { playerState ->
                Log.d("SavedMusicFragment", "Player State: ${playerState.track.uri}, Track ID: $trackId")
                if (playerState.track.uri == trackId) {
                    if (playerState.isPaused) {
                        playerApi.resume().setErrorCallback { error -> logError(error) }
                        Log.d("savedMusicFragment", "Resuming track: $trackId")
                    } else {
                        playerApi.pause().setErrorCallback { error -> logError(error) }
                        Log.d("savedMusicFragment", "Pausing track: $trackId")
                    }
                } else {
                    playerApi.play(trackId).setErrorCallback { error -> logError(error) }
                    Log.d("savedMusicFragment", "Playing new track: $trackId")
                }
            }.setErrorCallback { error -> logError(error) }
        }
    }


    private fun clickEditButton() {
        binding.btnEdit.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fcv_music, DeletedMusicFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    private fun logError(throwable: Throwable) {
        //Toast.makeText(requireContext(), R.string.err_generic_toast, Toast.LENGTH_SHORT).show()
        Log.e(PlayFragment.TAG, "", throwable)
    }

    fun setSpotifyAppRemote(remote: SpotifyAppRemote?) {
        this.spotifyAppRemote = remote
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}