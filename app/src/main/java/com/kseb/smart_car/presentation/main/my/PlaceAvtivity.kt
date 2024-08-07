package com.kseb.smart_car.presentation.main.my

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kseb.smart_car.databinding.ActivityPlaceBinding

class PlaceActivity: AppCompatActivity() {
    private lateinit var binding: ActivityPlaceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val placeAdapter = PlaceAdapter()
        binding.rvPlace.adapter = placeAdapter
    }
}