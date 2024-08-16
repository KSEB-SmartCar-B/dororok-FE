package com.kseb.smart_car.presentation.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.kseb.smart_car.R
import com.kseb.smart_car.data.service.SpotifyService.connect
import com.kseb.smart_car.databinding.FragmentLoginBinding
import com.kseb.smart_car.extension.AccessState
import com.kseb.smart_car.extension.SignInState
import com.kseb.smart_car.presentation.AllViewModel
import com.kseb.smart_car.presentation.KakaoAuthViewModel
import com.kseb.smart_car.presentation.KakaoAuthViewModelFactory
import com.kseb.smart_car.presentation.join.JoinActivity
import com.kseb.smart_car.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment: Fragment() {
    private var _binding:FragmentLoginBinding?=null
    private val binding:FragmentLoginBinding
        get() = requireNotNull(_binding){"null"}

    private lateinit var kakaoAuthViewModel: KakaoAuthViewModel
    private val allViewModel: AllViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       _binding=FragmentLoginBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setting()
        isLogin()
        clickButton()
    }

    private fun setting(){
        val factory = KakaoAuthViewModelFactory(requireActivity().application, allViewModel)
        kakaoAuthViewModel = ViewModelProvider(this, factory)[KakaoAuthViewModel::class.java]
    }

    private fun clickButton() {
        binding.btnLogin.setOnClickListener {
            kakaoAuthViewModel.kakaoLogin(requireActivity())
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

                    false -> Log.d("loginFragment", "login failed")
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
                        Log.d("loginFragment", "kakaoToken: ${kakaoToken}")
                        if (signInState.isSigned) {
                            Log.d("loginFragment", "signed!")
                            collectAccessState(kakaoToken!!)
                        } else {
                            Log.d("loginFragment", "is not signed!")
                            // KakaoToken을 가져와서 Intent에 추가
                            val intent = Intent(requireActivity(), JoinActivity::class.java)
                            intent.putExtra("kakaoToken", kakaoToken)
                            //intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
//                            requireActivity().finish() 회원가입에서 뒤로가기하면 로그인 안나와서 주석처리 했음
                            allViewModel.setSignInStateLoading()
                            Log.e("loginFragment", "회원가입 하시오!")
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
                        Log.d("loginFragment", "accessToken:${accessState.accessToken}")
                        connect(requireActivity()) {
                            if (it) {
                                val intent =
                                    Intent(requireActivity(), MainActivity::class.java).apply {
                                        putExtra("accessToken", "Bearer ${accessState.accessToken}")
                                        //addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                startActivity(intent)
                                requireActivity().finish()
                            } else {
                                Toast.makeText(
                                    requireActivity(),
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
}