package com.kseb.smart_car.presentation.main.my.music

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.ActivityMymusicBinding
import com.kseb.smart_car.presentation.main.my.place.SavedplaceFragment

class MymusicActivity: AppCompatActivity() {

    private lateinit var binding: ActivityMymusicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMymusicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(SavedmusicFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fcv_music, fragment)
            .commit()
    }
}