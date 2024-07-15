package com.kseb.smart_car.presentation.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.ActivityLoginBinding
import com.kseb.smart_car.extension.AccessState
import com.kseb.smart_car.presentation.AllViewModel
import com.kseb.smart_car.presentation.KakaoAuthViewModel
import com.kseb.smart_car.presentation.KakaoAuthViewModelFactory
import com.kseb.smart_car.presentation.join.JoinActivity
import com.kseb.smart_car.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding:ActivityLoginBinding
    private lateinit var kakaoAuthViewModel: KakaoAuthViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBinds()
        setting()
        clickButton()
    }

    private fun initBinds(){
        binding=ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setting(){
        val factory = KakaoAuthViewModelFactory(application)
        kakaoAuthViewModel = ViewModelProvider(this, factory).get(KakaoAuthViewModel::class.java)
        //prefs = getSharedPreferences("tokenPrefs", Context.MODE_PRIVATE)
        //isLogin()
    }

    private fun clickButton() {
        binding.btnLogin.setOnClickListener {
            kakaoAuthViewModel.kakaoLogin()
        }

        lifecycleScope.launch {
            kakaoAuthViewModel.isLoggedIn.collect{
                when(it){
                    true -> {
                        val intent=Intent(this@LoginActivity, JoinActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    false -> {
                        Log.e("Failed","카카오 계정으로 로그인 실패")
                    }
                }
            }
        }
    }

   /* private fun isLogin(){
        lifecycleScope.launch {
            kakaoAuthViewModel.isLoggedIn.collect{
                when(it){
                    true -> {
                        collectAccessState()
                    }
                    false -> Log.d("startactivity","login failed")
                }
            }
        }
    }

    private fun collectAccessState(){
        lifecycleScope.launch {
            allViewModel.accessState.collect{accessState ->
                when(accessState){
                    is AccessState.Success -> {
                        Log.d("startactivity","token: ${accessState.accessToken}")
                        val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                            putExtra("accessToken",accessState.accessToken)
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                        finish()
                    }
                    is AccessState.Error ->{
                        Log.e("startactivity","token doesn't exist!! ${accessState.message}")
                    }

                    is AccessState.Loading->{

                    }
                }
            }
        }
    }*/

    override fun onDestroy() {
        super.onDestroy()
    }

}