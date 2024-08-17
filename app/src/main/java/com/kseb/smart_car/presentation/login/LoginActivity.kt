package com.kseb.smart_car.presentation.login

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.PathMeasure
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
import com.kseb.smart_car.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.io.path.Path

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var kakaoAuthViewModel: KakaoAuthViewModel
    private val allViewModel: AllViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()

    private lateinit var logo: ImageView
    private lateinit var road: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBinds()
        setting()
        //clickButton()
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

        /*val factory = KakaoAuthViewModelFactory(application, allViewModel)
        kakaoAuthViewModel = ViewModelProvider(this, factory).get(KakaoAuthViewModel::class.java)*/
        //prefs = getSharedPreferences("tokenPrefs", Context.MODE_PRIVATE)

        replaceFragment()
        animatePage()
       // isLogin()
    }

    private fun animatePage() {
        logo = binding.ivDororokLogo
        road = binding.ivBackground

        road.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                road.viewTreeObserver.removeOnGlobalLayoutListener(this)
                animatePin()
            }
        })
    }

    private fun animatePin() {
        val path = android.graphics.Path().apply {
            moveTo(700f, 100f)
            quadTo(680f,120f,350f,300f)
            quadTo(350f, 300f, 1500f, 400f)
            quadTo(380f,500f,-400f,750f)
            quadTo(-200f, 1000f, 380f, 1350f)
        }

        val pathMeasure = PathMeasure(path, false)
        val pathLength = pathMeasure.length

        var scale=0.0f
        val positionAnimator = ValueAnimator.ofFloat(0f, pathMeasure.length)
        positionAnimator.duration = 3300
        positionAnimator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            val pos = FloatArray(2)
            pathMeasure.getPosTan(value, pos, null)
            logo.translationX = pos[0]
            logo.translationY = pos[1]

            // 핀 크기 업데이트
            scale = 1.5f + (value / pathLength)
            logo.scaleX = scale
            logo.scaleY = scale
        }

        positionAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // 애니메이션 끝난 후 road와 logo를 위로 500f 이동하고, logo의 크기를 1.3배로 키움
                road.animate().translationYBy(-450f).setDuration(1000).start()
                logo.animate()
                    .translationYBy(-450f)
                    .scaleX(scale*1.3f)
                    .scaleY(scale*1.3f)
                    .setDuration(1000)
                    .start()

                with(binding){
                    //-300dp를 픽셀로 변환
                    val marginBottompx=TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,-300f,resources.displayMetrics
                    )

                    fcvLogin.visibility=View.VISIBLE
                    fcvLogin.animate().translationYBy(marginBottompx).setDuration(1000).start()

                    // tvLogoName을 페이드 인 애니메이션
                    tvDororokName.visibility=View.VISIBLE
                    tvDororokName.animate()
                        .alpha(1f)
                        .setDuration(1000)
                        .start()
                }
            }
        })
        positionAnimator.start()
    }

    private fun replaceFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fcv_login, LoginFragment())
            .commit()
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}