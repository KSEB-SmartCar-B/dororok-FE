package com.kseb.smart_car.presentation.main.my

import android.icu.util.Calendar
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.kseb.smart_car.data.responseDto.ResponseMyInfoDto
import com.kseb.smart_car.databinding.ActivityInformationBinding
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.LocalDate

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
        val jsonString=intent.getStringExtra("info")
        val infoDto=jsonString?.let{
            Json.decodeFromString<ResponseMyInfoDto>(it) }

        val gender = infoDto!!.gender
        val nickname = infoDto!!.nickname
        val birthday=infoDto!!.birthday

        if (gender== "MALE") {
            binding.btnMale.isSelected = true
        } else binding.btnFemale.isSelected = true

        binding.etNick.setText(nickname)

        setDate(birthday)
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
    private fun setDate(birthday: LocalDate) {
        val datePicker = binding.dpSpinner
        val calendar = Calendar.getInstance()

        var birthYear = birthday.year
        var birthMonth = birthday.month.value
        var birthDay = birthday.dayOfMonth

        var today_year = calendar.get(Calendar.YEAR)
        var today_month = calendar.get(Calendar.MONTH)
        var today_day = calendar.get(Calendar.DAY_OF_MONTH)

        // 시작 날짜
        datePicker.init(
            birthYear,
            birthMonth,
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
            "MALE"
        } else {
            "FEMALE"
        }

        val nickname = binding.etNick.text.toString()

        val birthYear = binding.dpSpinner.year
        val birthMonth = binding.dpSpinner.month
        val birthDay = binding.dpSpinner.dayOfMonth

        val birth=LocalDate.of(birthYear,birthMonth,birthDay)

        viewmodel.updateInfo()
    }

    private fun clickButtonOk() {
        binding.btnOk.setOnClickListener {
            setInfo()
            finish()
        }
    }
}