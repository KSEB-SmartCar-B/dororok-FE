package com.kseb.smart_car.presentation.main.my.place

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.ActivityMyplaceBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyplaceActivity: AppCompatActivity() {

    private lateinit var binding: ActivityMyplaceBinding
    private val savedPlaceViewModel:SavedPlaceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyplaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        savedPlaceViewModel.setToken(intent.getStringExtra("accessToken")!!)
        replaceFragment(SavedplaceFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fcv_place, fragment)
            .commit()
    }
}