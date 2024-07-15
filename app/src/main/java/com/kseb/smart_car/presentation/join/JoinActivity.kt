package com.kseb.smart_car.presentation.join

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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

        replaceFragment(JoinFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fcv_join, fragment)
            .commit() }

//    private fun clickButton() {
//        binding.btnJoin.setOnClickListener {
//            if(true){
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
//                finish()
//            }
//        }
//    }
}