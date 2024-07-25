package com.kseb.smart_car.presentation.join

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.ActivityJoinBinding

class JoinActivity: AppCompatActivity() {
    private lateinit var binding: ActivityJoinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fcv_join, JoinInfoFragment())
            .commit()
    }
}