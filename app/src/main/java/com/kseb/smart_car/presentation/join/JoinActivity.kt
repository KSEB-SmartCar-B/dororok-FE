package com.kseb.smart_car.presentation.join

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.ActivityJoinBinding
import com.kseb.smart_car.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class JoinActivity: AppCompatActivity() {
    private lateinit var binding: ActivityJoinBinding
    private val joinViewModel:JoinViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val token=intent.getStringExtra("kakaoToken")
        Log.d("joinactivity","kakaotoken:${token}")
        joinViewModel.setKakaoToken(token!!)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fcv_join, JoinInfoFragment())
            .commit()
    }

    override fun onBackPressed() {
        super.onBackPressed() // Call the default implementation to finish the activity
        // Optionally, you can add custom behavior here if needed
        finish()
    }
}