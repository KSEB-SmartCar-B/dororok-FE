package com.kseb.smart_car.presentation.main.my

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.kseb.smart_car.databinding.ActivityGenreBinding
import com.kseb.smart_car.presentation.join.JoinGenreViewModel

class GenreActivity: AppCompatActivity() {
    private lateinit var binding: ActivityGenreBinding

    private val myviewmodel by viewModels<MyViewModel>()
    private val joinviewmodel by viewModels<JoinGenreViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGenreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*val genreAdapter = GenreAdapter {buttonText -> viewmodel.getGenre(buttonText)}
=======
        val genreAdapter = GenreAdapter {buttonText -> myviewmodel.getGenre(buttonText)}
>>>>>>> 2419c74f8fca0d822d337e7b24f4789dc4d43cbf
        binding.rvGenre.adapter = genreAdapter

        genreAdapter.getList(joinviewmodel.makeList())

        binding.rvGenre.layoutManager = GridLayoutManager(this, 3)

        clickButtonOk()*/
    }

    private fun clickButtonOk() {
        binding.btnOk.setOnClickListener {
            finish()
        }
    }
}