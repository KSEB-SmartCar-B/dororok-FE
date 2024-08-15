package com.kseb.smart_car.presentation.main.map.navi

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.kseb.smart_car.databinding.FragmentLoadingBinding

class LoadingDialogFragment : DialogFragment() {

    private var _binding: FragmentLoadingBinding? = null
    private val binding get() = _binding!!

    private val dots by lazy {
        listOf(binding.fitstDot, binding.secondDot, binding.thirdDot)
    }
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWindowAttributes()
        startLoadingAnimation()
    }

    // Dialog Window 속성을 구성
    private fun setUpWindowAttributes() {
        val params = dialog?.window?.attributes
        params?.width = ViewGroup.LayoutParams.MATCH_PARENT
        params?.height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog?.window?.attributes = params
        dialog?.window?.setBackgroundDrawableResource(android.R.color.white)
    }

    // 점 이미지뷰의 반복 애니메이션을 시작
    private fun startLoadingAnimation() {
        var dotIndex = 0
        handler.post(object : Runnable {
            override fun run() {
                animateDot(dots[dotIndex])
                dotIndex = (dotIndex + 1) % dots.size
                handler.postDelayed(this, 500) // 500ms 간격으로 변경
            }
        })
    }

    // 점 이미지뷰의 크기 조정 애니메이션을 수행
    private fun animateDot(dot: ImageView) {
        val scaleX = ObjectAnimator.ofFloat(dot, "scaleX", 1.0f, 1.5f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(dot, "scaleY", 1.0f, 1.5f, 1.0f)

        /**
         * AccelerateDecelerateInterpolator : AccelerateInterpolator와 DecelerateInterpolator를 합친 효과를 나타낸다.
         * AccelerateInterpolator : 시작지점에서 가속하여 종료지점에 도달한다.
         * DecelerateInterpolator : 종료지점에 도달할 수록 속도가 느려진다.
         */
        scaleX.interpolator = AccelerateDecelerateInterpolator()
        scaleY.interpolator = AccelerateDecelerateInterpolator()

        scaleX.duration = 500 // 애니메이션 지속 시간 (ms)
        scaleY.duration = 500 // 애니메이션 지속 시간 (ms)

        scaleX.start()
        scaleY.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}