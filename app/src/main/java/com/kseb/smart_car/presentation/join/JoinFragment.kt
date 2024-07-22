package com.kseb.smart_car.presentation.join

import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.FragmentJoinBinding

class JoinFragment : Fragment() {
    private var _binding: FragmentJoinBinding? = null
    private val binding: FragmentJoinBinding
        get() = requireNotNull(_binding) { "null" }

    private val viewmodel by viewModels<JoinViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJoinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        genderButton()
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
        }

        btnFemale.setOnClickListener {
            btnFemale.isSelected = true
            btnMale.isSelected = false
        }
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
            setInfo()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fcv_join, Join2Fragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }
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

        viewmodel.getInfo(gender, nickname, birthYear, birthMonth, birthDay)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}