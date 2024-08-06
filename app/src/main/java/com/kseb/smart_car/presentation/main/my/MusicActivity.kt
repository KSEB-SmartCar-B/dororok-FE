package com.kseb.smart_car.presentation.main.my

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kseb.smart_car.databinding.ActivityGenreBinding
import com.kseb.smart_car.databinding.ActivityMusicBinding

class MusicActivity: AppCompatActivity() {
    private lateinit var binding: ActivityMusicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val musicAdapter = MusicAdapter()
        binding.rvMusic.adapter = musicAdapter
    }
}