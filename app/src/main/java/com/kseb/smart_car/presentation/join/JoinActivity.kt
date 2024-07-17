package com.kseb.smart_car.presentation.join

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.ActivityJoinBinding
import com.kseb.smart_car.presentation.main.MainActivity
import kotlinx.coroutines.launch

class JoinActivity: AppCompatActivity() {
    private lateinit var binding: ActivityJoinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fcv_join, JoinFragment())
            .commit()
    }
}