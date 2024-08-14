package com.kseb.smart_car.presentation.main.my.music

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.ActivityMymusicBinding
import com.kseb.smart_car.presentation.BaseActivity
import com.kseb.smart_car.presentation.SpotifyRemoteManager.spotifyAppRemote
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyMusicActivity: AppCompatActivity() {

    private lateinit var binding: ActivityMymusicBinding
    private val savedMusicViewModel:SavedMusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMymusicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val token=intent.getStringExtra("accessToken")!!
        savedMusicViewModel.setAccessToken(token)

        val savedMusicFragment=SavedMusicFragment()
        savedMusicFragment.setSpotifyAppRemote(spotifyAppRemote)
        if(spotifyAppRemote==null){
            Log.e("myMusicActivity","spotifyAppRemote is null")
        }else{
            Log.e("myMusicActivity","spotifyAppRemote is not null")
        }
        replaceFragment(savedMusicFragment)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fcv_music, fragment)
            .commit()
    }
}