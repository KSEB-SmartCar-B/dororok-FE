package com.kseb.smart_car.presentation.main.my

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.kseb.smart_car.databinding.ActivityGenreBinding
import com.kseb.smart_car.presentation.join.Join2ViewModel

class GenreActivity: AppCompatActivity() {
    private lateinit var binding: ActivityGenreBinding

    private val viewmodel by viewModels<MyViewModel>()
    private val viewmodel2 by viewModels<Join2ViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGenreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*val genreAdapter = GenreAdapter {buttonText -> viewmodel.getGenre(buttonText)}
        binding.rvGenre.adapter = genreAdapter

        genreAdapter.getList(viewmodel2.makeList())

        binding.rvGenre.layoutManager = GridLayoutManager(this, 3)

        clickButtonOk()*/
    }

    private fun clickButtonOk() {
        binding.btnOk.setOnClickListener {
            finish()
        }
    }
}