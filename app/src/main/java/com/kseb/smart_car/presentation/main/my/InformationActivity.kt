package com.kseb.smart_car.presentation.main.my

import android.app.Activity
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.kseb.smart_car.data.requestDto.RequestUpdateInfoDto
import com.kseb.smart_car.data.responseDto.ResponseMyInfoDto
import com.kseb.smart_car.databinding.ActivityInfoBinding
import com.kseb.smart_car.extension.UpdateInfoState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@AndroidEntryPoint
class InformationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInfoBinding
    private val infoViewModel by viewModels<InfoViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setting()
        genderButton()
        clickButtonOk()
    }

    private fun setting() {
        val jsonString = intent.getStringExtra("info")
        val infoDto = jsonString?.let {
            Json.decodeFromString<ResponseMyInfoDto>(it)
        }

        val gender = infoDto!!.gender
        val nickname = infoDto!!.nickname
        val birthday = infoDto!!.birthday

        if (gender == "MALE") {
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
        var birthMonth = birthday.month.value - 1
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

    private fun clickButtonOk() {
        binding.btnOk.setOnClickListener {
            lifecycleScope.launch {
                val isUpdated = updateInfo()
                if (isUpdated) {
                    // 결과 반환 설정
                    // Broadcast 전송
                    val updateIntent = Intent("com.example.UPDATE_INFO")
                    LocalBroadcastManager.getInstance(this@InformationActivity).sendBroadcast(updateIntent)

                    Log.d("informationActivity","finish informationActivity!")
                    finish()
                } else {
                    Log.e("informationActivity", "정보 업데이트 실패!")
                }
            }
        }
    }

    private suspend fun updateInfo(): Boolean {
        val gender = if (binding.btnMale.isSelected) {
            "MALE"
        } else {
            "FEMALE"
        }

        val nickname = binding.etNick.text.toString()

        val birthYear = binding.dpSpinner.year
        val birthMonth = binding.dpSpinner.month+1
        val birthDay = binding.dpSpinner.dayOfMonth

        val birth = LocalDate.of(birthYear, birthMonth, birthDay)

        val accessToken = intent.getStringExtra("accessToken")

        Log.d("informationactivity","nickname: ${nickname}, birthday:${birth}, gender: ${gender}, token: ${accessToken}")
        infoViewModel.updateInfo(accessToken!!, RequestUpdateInfoDto(nickname, birth, gender))

        return suspendCoroutine { continuation ->
            lifecycleScope.launch {
                infoViewModel.updateInfoState.collect { updateState ->
                    when (updateState) {
                        is UpdateInfoState.Success -> {
                            continuation.resume(true)
                        }

                        is UpdateInfoState.Loading -> {}
                        is UpdateInfoState.Error -> {
                            continuation.resume(false)
                        }
                    }
                }
            }
        }
    }
}