package com.kseb.smart_car.presentation.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.kseb.smart_car.R
import com.kseb.smart_car.data.service.SpotifyService.connect
import com.kseb.smart_car.databinding.ActivityLoginBinding
import com.kseb.smart_car.extension.AccessState
import com.kseb.smart_car.extension.SignInState
import com.kseb.smart_car.presentation.AllViewModel
import com.kseb.smart_car.presentation.KakaoAuthViewModel
import com.kseb.smart_car.presentation.KakaoAuthViewModelFactory
import com.kseb.smart_car.presentation.join.JoinActivity
import com.kseb.smart_car.presentation.main.LocationActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var kakaoAuthViewModel: KakaoAuthViewModel
    private val allViewModel: AllViewModel by viewModels()
    private val loginViewModel:LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBinds()
        setting()
        clickButton()
    }

    private fun initBinds() {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setting() {
        val window = window
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        val factory = KakaoAuthViewModelFactory(application, allViewModel)
        kakaoAuthViewModel = ViewModelProvider(this, factory).get(KakaoAuthViewModel::class.java)
        //prefs = getSharedPreferences("tokenPrefs", Context.MODE_PRIVATE)

        isLogin()
    }

    private fun clickButton() {
        binding.btnLogin.setOnClickListener {
            kakaoAuthViewModel.kakaoLogin()
        }

        /* lifecycleScope.launch {
             kakaoAuthViewModel.isLoggedIn.collect{
                 when(it){
                     true -> {
                         val intent=Intent(this@LoginActivity, MainActivity::class.java)
                         startActivity(intent)
                         finish()
                     }
                     false -> {
                         Log.e("Failed","카카오 계정으로 로그인 실패")
                     }
                 }
             }
         }*/
    }

    private fun isLogin() {
        lifecycleScope.launch {
            kakaoAuthViewModel.isLoggedIn.collect {
                when (it) {
                    true -> {
                        isSigned()
                    }
                    false -> Log.d("startactivity", "login failed")
                }
            }
        }
    }

    private fun isSigned() {
        lifecycleScope.launch {
            allViewModel.signInState.collect { signInState ->
                when (signInState) {
                    is SignInState.Success -> {
                        val kakaoToken = allViewModel.kakaoToken.value
                        Log.d("loginactivity","kakaoToken: ${kakaoToken}")
                        if (signInState.isSigned) {
                            Log.d("loginactivity", "signed!")
                            collectAccessState(kakaoToken!!)
                        } else {
                            Log.d("loginactivity", "is not signed!")
                            // KakaoToken을 가져와서 Intent에 추가
                            val intent = Intent(this@LoginActivity, JoinActivity::class.java)
                            intent.putExtra("kakaoToken",kakaoToken)
                            //intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                            allViewModel.setSignInStateLoading()
                            Log.e("loginactivity", "회원가입 하시오!")
                        }
                    }

                    is SignInState.Error -> {

                    }

                    is SignInState.Loading -> {

                    }
                }
            }
        }
    }

    private fun collectAccessState(token: String) {
        loginViewModel.getAccessToken(token)
        lifecycleScope.launch {
            loginViewModel.accessState.collect { accessState ->
                when (accessState) {
                    is AccessState.Success -> {
                        Log.d("loginactivity", "accessToken:${accessState.accessToken}")
                        connect(this@LoginActivity) {
                            if(it){
                                val intent = Intent(this@LoginActivity, LocationActivity::class.java).apply {
                                    putExtra("accessToken", "Bearer ${accessState.accessToken}")
                                    //addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                startActivity(intent)
                            }else {
                                Toast.makeText(
                                    this@LoginActivity,
                                    R.string.spotify_error,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    is AccessState.Error -> {
                        Log.e("loginactivity", "token doesn't exist!! ${accessState.message}")
                    }

                    is AccessState.Loading -> {

                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}