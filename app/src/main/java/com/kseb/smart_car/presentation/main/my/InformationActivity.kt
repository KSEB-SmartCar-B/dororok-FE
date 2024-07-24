package com.kseb.smart_car.presentation.main.my

import android.icu.util.Calendar
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.ActivityInformationBinding
import com.kseb.smart_car.databinding.ActivityJoinBinding
import com.kseb.smart_car.presentation.join.JoinFragment
import com.kseb.smart_car.presentation.join.JoinViewModel

class InformationActivity: AppCompatActivity() {
    private lateinit var binding: ActivityInformationBinding
    private val viewmodel by viewModels<MyViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setting()
        genderButton()
        clickButtonOk()
    }

    private fun setting() {
        val gender = viewmodel.gender
        val nickname = viewmodel.nickname

        if (gender=="남자") {
            binding.btnMale.isSelected = true
        } else binding.btnFemale.isSelected = true

        binding.etNick.setText(nickname)

        setDate()
    }

    //성별 버튼 선택했을 때 색칠되는 함수
    private fun genderButton() {
        val btnMale = binding.btnMale
        val btnFemale = binding.btnFemale

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

        var birthYear = viewmodel.birthYear.toInt()
        var birthMonth = viewmodel.birthMonth.toInt()
        var birthDay = viewmodel.birthDay.toInt()

        var today_year = calendar.get(Calendar.YEAR)
        var today_month = calendar.get(Calendar.MONTH)
        var today_day = calendar.get(Calendar.DAY_OF_MONTH)

        // 시작 날짜
        datePicker.init(
            birthYear,
            birthMonth-1,
            birthDay
        ) { _, _, _, _ -> }

        // 최소 날짜
        calendar.set(1950, Calendar.JANUARY, 1)
        datePicker.minDate = calendar.timeInMillis

        // 최대 날짜
        calendar.set(today_year, today_month, today_day)
        datePicker.maxDate = calendar.timeInMillis
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

    private fun clickButtonOk() {
        binding.btnOk.setOnClickListener {
            setInfo()
            finish()
        }
    }
}