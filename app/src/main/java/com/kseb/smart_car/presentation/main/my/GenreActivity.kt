package com.kseb.smart_car.presentation.main.my

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.kseb.smart_car.databinding.ActivitySavedgenreBinding
import com.kseb.smart_car.extension.AllGenreState
import com.kseb.smart_car.extension.GenreState
import com.kseb.smart_car.extension.UpdateGenreState
import com.kseb.smart_car.presentation.join.JoinGenreViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GenreActivity: AppCompatActivity() {
    private lateinit var binding: ActivitySavedgenreBinding

    private val genreViewModel by viewModels<GenreViewModel>()
    private val joinGenreViewModel by viewModels<JoinGenreViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySavedgenreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val token = intent.getStringExtra("token")
        genreViewModel.getMyGenre(token!!)

        lifecycleScope.launch {
            genreViewModel.genreState.collect{genreState ->
                when(genreState){
                    is GenreState.Success -> {
                        val genreAdapter = GenreAdapter {buttonText -> genreViewModel.getGenre(buttonText)}
                        binding.rvGenre.adapter = genreAdapter

                        joinGenreViewModel.getGenreList()
                        lifecycleScope.launch {
                            joinGenreViewModel.genreListState.collect{allGenreState ->
                                when(allGenreState){
                                    is AllGenreState.Success -> {
                                        genreAdapter.getList(allGenreState.genreDto.names)

                                        genreAdapter.getMyList(genreState.genreDto.favoriteGenres)

                                        binding.rvGenre.layoutManager = GridLayoutManager(this@GenreActivity, 3)

                                        clickButtonOk(token)
                                    }
                                    is AllGenreState.Loading->{}
                                    is AllGenreState.Error -> {
                                        Log.e("genreActivity","장르 불러오기 실패")
                                    }
                                }
                            }
                        }

                    }
                    is GenreState.Loading ->{}
                    is GenreState.Error -> {
                        Log.e("GenreActivity","정보 가져오기 실패..: ${genreState.message} ")
                    }
                }
            }
        }
    }

    private fun clickButtonOk(token:String) {
        binding.btnOk.setOnClickListener {
            genreViewModel.setGenre(token)
            lifecycleScope.launch {
                genreViewModel.updateGenreState.collect{updateGenreState->
                    when(updateGenreState){
                        is UpdateGenreState.Success -> {
                            finish()
                        }
                        is UpdateGenreState.Loading->{}
                        is UpdateGenreState.Error->{
                            Log.e("genreActivity","update genre error: ${updateGenreState.message}")
                        }
                    }
                }
            }

        }
    }
}