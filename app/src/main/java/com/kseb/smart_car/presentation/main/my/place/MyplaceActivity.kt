package com.kseb.smart_car.presentation.main.my.place

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.ActivityMyplaceBinding

class MyplaceActivity: AppCompatActivity() {

    private lateinit var binding: ActivityMyplaceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyplaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(SavedplaceFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fcv_place, fragment)
            .commit()
    }
}