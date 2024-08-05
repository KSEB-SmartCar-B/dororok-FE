package com.kseb.smart_car.presentation.join

import android.icu.util.Calendar
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.FragmentJoinInfoBinding

class JoinInfoFragment: Fragment() {
    private var _binding: FragmentJoinInfoBinding? = null
    private val binding: FragmentJoinInfoBinding
        get() = requireNotNull(_binding) { "null" }

    private val joinViewModel: JoinViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJoinInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        genderButton()
        nickButton()
        setDate()
        clickButtonNext()

    }

    //성별 버튼 선택했을 때 색칠되는 함수
    private fun genderButton() {
        val btnMale = binding.btnMale
        val btnFemale = binding.btnFemale

        btnMale.isSelected = false
        btnFemale.isSelected = false

        // 버튼 클릭 이벤트 설정
        btnMale.setOnClickListener {
            btnMale.isSelected = true
            btnFemale.isSelected = false
            binding.gender.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.border_default)
        }

        btnFemale.setOnClickListener {
            btnFemale.isSelected = true
            btnMale.isSelected = false
            binding.gender.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.border_default)
        }
    }

    //닉네임 칸에 뭔가 입력하면 빨간 테두리 사라지도록
    private fun nickButton() {
        binding.etNick.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank()) {
                    binding.nickname.background = ContextCompat.getDrawable(requireContext(), R.drawable.border_default)
                }
            }
        })
    }

    //생년월일 선택할 때 날짜범위 및 시작날짜 설정하는 함수
    private fun setDate() {
        val datePicker = binding.dpSpinner
        val calendar = Calendar.getInstance()

        var today_year = calendar.get(Calendar.YEAR)
        var today_month = calendar.get(Calendar.MONTH)
        var today_day = calendar.get(Calendar.DAY_OF_MONTH)

        // 시작 날짜 (오늘 날짜)
        datePicker.init(
            today_year,
            today_month,
            today_day
        ) { _, _, _, _ -> }

        // 최소 날짜
        calendar.set(1950, Calendar.JANUARY, 1)
        datePicker.minDate = calendar.timeInMillis

        // 최대 날짜
        calendar.set(today_year, today_month, today_day)
        datePicker.maxDate = calendar.timeInMillis
    }

    private fun clickButtonNext() {
        binding.btnNext.setOnClickListener {
            if (checkInfo()) {
                setInfo()
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fcv_join, JoinGenreFragment())
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }
    }

    //입력한 정보 유효한지 췤
    private fun checkInfo(): Boolean {
        var isValid = true

        if (!binding.btnMale.isSelected && !binding.btnFemale.isSelected) {
            binding.gender.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.border_red)
            isValid = false
        }

        if (binding.etNick.text.toString().trim().isEmpty()) {
            binding.nickname.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.border_red)
            isValid = false
        }

        return isValid
    }


    private fun setInfo() {
        val gender = if (binding.btnMale.isSelected) {
            "male"
        } else {
            "female"
        }

        val nickname = binding.etNick.text.toString()

        val birthYear = binding.dpSpinner.year.toString()
        val birthMonth = binding.dpSpinner.month.toString()
        val birthDay = binding.dpSpinner.dayOfMonth.toString()

        joinViewModel.getInfo(gender, nickname, birthYear, birthMonth, birthDay)
    }

    override fun onStart() {
        super.onStart()
        Log.d("JoinInfoFragment", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("JoinInfoFragment", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("JoinInfoFragment", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("JoinInfoFragment", "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("JoinInfoFragment", "onDestroy")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}