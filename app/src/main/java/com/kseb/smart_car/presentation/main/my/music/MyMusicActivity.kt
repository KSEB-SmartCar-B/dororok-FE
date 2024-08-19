package com.kseb.smart_car.presentation.main.my.music

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.ActivityMymusicBinding
import com.kseb.smart_car.extension.DeleteMusicListState
import com.kseb.smart_car.extension.GetFavoriteMusicState
import com.kseb.smart_car.presentation.BaseActivity
import com.kseb.smart_car.presentation.SpotifyRemoteManager.spotifyAppRemote
import com.kseb.smart_car.presentation.main.music.PlayFragment
import com.kseb.smart_car.presentation.main.music.PlayViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyMusicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMymusicBinding
    private val savedMusicViewModel: SavedMusicViewModel by viewModels()
    private val playViewModel: PlayViewModel by viewModels()
    private lateinit var savedMusicAdapter: SavedMusicAdapter

    private var editClick: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBind()
        setting()
        /* val savedMusicFragment=SavedMusicFragment()
         savedMusicFragment.setSpotifyAppRemote(spotifyAppRemote)
         if(spotifyAppRemote==null){
             Log.e("myMusicActivity","spotifyAppRemote is null")
         }else{
             Log.e("myMusicActivity","spotifyAppRemote is not null")
         }*/


        //replaceFragment(savedMusicFragment)
    }

    private fun initBind() {
        binding = ActivityMymusicBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setting() {
        val token = intent.getStringExtra("accessToken")!!
        savedMusicViewModel.setAccessToken(token)

        savedMusicAdapter = SavedMusicAdapter({
            trackId -> playOrPauseMusic(trackId)
            Log.d("savedMusicFragment", "music click!")
        }, { deleteMusicList -> deleteMusic(deleteMusicList) })
        binding.rvMusic.adapter = savedMusicAdapter
        savedMusicAdapter.attachToRecyclerView(binding.rvMusic)

        setAccesstoken()
        clickButton()
    }

    private fun playOrPauseMusic(trackId: String) {
        if (spotifyAppRemote == null) {
            Log.d("savedMusicFragment", "spotifyAppRemote is null")
            return
        }

        spotifyAppRemote?.playerApi?.let { playerApi ->
            playerApi.playerState.setResultCallback { playerState ->
                Log.d(
                    "SavedMusicFragment",
                    "Player State: ${playerState.track.uri}, Track ID: $trackId"
                )
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

    private fun deleteMusic(trackId: List<String>) {
        savedMusicViewModel.deleteMusicList(trackId)
        lifecycleScope.launch {
            savedMusicViewModel.deleteMusicListState.collect{state->
                when(state){
                    is DeleteMusicListState.Success->{
                        Log.d("myMusicActivity","delete music list state success")
                        savedMusicViewModel.setDeleteMusicListStateLoading()
                    }
                    is DeleteMusicListState.Loading->{}
                    is DeleteMusicListState.Error->{
                        Log.d("myMusicActivity","delete music list state error: ${state.message}")
                    }
                }
            }
        }
    }

    private fun setAccesstoken() {
        lifecycleScope.launch {
            savedMusicViewModel.accessToken.observe(this@MyMusicActivity) { token ->
                playViewModel.setAccessToken(token)
                Log.d("savedMusicFragment", "get accessToken")
                getSavedMusic()
            }
        }
    }

    private fun getSavedMusic() {
        playViewModel.getFavoriteMusicList()
        lifecycleScope.launch {
            playViewModel.favoriteMusicState.collect { favoriteMusicState ->
                when (favoriteMusicState) {
                    is GetFavoriteMusicState.Success -> {
                        savedMusicAdapter.setMusicList(favoriteMusicState.favoriteMusicDto.favoritesMusicList)
                    }

                    is GetFavoriteMusicState.Loading -> {}
                    is GetFavoriteMusicState.Error -> {
                        Log.e("savedMusicFragment", "error:${favoriteMusicState.message}")
                    }
                }
            }
        }

    }

    private fun clickButton() {
        binding.btnEdit.setOnClickListener {
            if (editClick) {
                editClick = false
                binding.btnEdit.text = getText(R.string.my_edit)
            } else {
                editClick = true
                binding.btnEdit.text = getText(R.string.my_delete)
            }
            savedMusicAdapter.toggleEditMode(editClick)
        }

    }

    private fun logError(throwable: Throwable) {
        //Toast.makeText(requireContext(), R.string.err_generic_toast, Toast.LENGTH_SHORT).show()
        Log.e(PlayFragment.TAG, "", throwable)
    }

    /* private fun replaceFragment(fragment: Fragment) {
         supportFragmentManager.beginTransaction()
             .replace(R.id.fcv_music, fragment)
             .commit()
     }*/
}