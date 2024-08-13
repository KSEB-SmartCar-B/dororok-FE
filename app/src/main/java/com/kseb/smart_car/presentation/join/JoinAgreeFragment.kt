package com.kseb.smart_car.presentation.join

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.kseb.smart_car.databinding.FragmentJoinAgreeBinding
import com.kseb.smart_car.presentation.join.agree.AgreeInfoFragment
import com.kseb.smart_car.presentation.join.agree.AgreeLocationFragment
import com.kseb.smart_car.presentation.join.agree.AgreeServiceFragment
import com.kseb.smart_car.presentation.join.agree.AgreeTransferFragment


class JoinAgreeFragment : Fragment() {
    private var _binding: FragmentJoinAgreeBinding? = null
    private val binding: FragmentJoinAgreeBinding
        get() = requireNotNull(_binding) { "null" }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJoinAgreeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clickAgreeButton()
        clickButtonDetail()
        clickButtonNext()
    }

    fun updateAllButton() {
        binding.btnAll.isSelected =
            binding.btnAgree1.isSelected && binding.btnAgree2.isSelected && binding.btnAgree3.isSelected && binding.btnAgree4.isSelected
    }

    //약관동의 버튼에 대한 함수
    private fun clickAgreeButton() {
        val all = binding.btnAll
        val agree1 = binding.btnAgree1
        val agree2 = binding.btnAgree2
        val agree3 = binding.btnAgree3
        val agree4 = binding.btnAgree4

        agree1.setOnClickListener {
            agree1.isSelected = !agree1.isSelected
            if (all.isSelected) all.isSelected = !all.isSelected
            updateAllButton()
            binding.agree1.background = ContextCompat.getDrawable(requireContext(), com.kseb.smart_car.R.drawable.border_default)
        }
        agree2.setOnClickListener {
            agree2.isSelected = !agree2.isSelected
            if (all.isSelected) all.isSelected = !all.isSelected
            updateAllButton()
            binding.agree2.background = ContextCompat.getDrawable(requireContext(), com.kseb.smart_car.R.drawable.border_default)
        }
        agree3.setOnClickListener {
            agree3.isSelected = !agree3.isSelected
            if (all.isSelected) all.isSelected = !all.isSelected
            updateAllButton()
            binding.agree3.background = ContextCompat.getDrawable(requireContext(), com.kseb.smart_car.R.drawable.border_default)
        }
        agree4.setOnClickListener {
            agree4.isSelected = !agree4.isSelected
            if (all.isSelected) all.isSelected = !all.isSelected
            updateAllButton()
            binding.agree4.background = ContextCompat.getDrawable(requireContext(), com.kseb.smart_car.R.drawable.border_default)
        }

        all.setOnClickListener {
            if (!all.isSelected) {
                agree1.isSelected = true
                agree2.isSelected = true
                agree3.isSelected = true
                agree4.isSelected = true
            } else {
                agree1.isSelected = false
                agree2.isSelected = false
                agree3.isSelected = false
                agree4.isSelected = false
            }
            all.isSelected = !all.isSelected
            binding.agree1.background = ContextCompat.getDrawable(requireContext(), com.kseb.smart_car.R.drawable.border_default)
            binding.agree2.background = ContextCompat.getDrawable(requireContext(), com.kseb.smart_car.R.drawable.border_default)
            binding.agree3.background = ContextCompat.getDrawable(requireContext(), com.kseb.smart_car.R.drawable.border_default)
            binding.agree4.background = ContextCompat.getDrawable(requireContext(), com.kseb.smart_car.R.drawable.border_default)
        }
    }

    private fun clickButtonNext() {
        binding.btnNext.setOnClickListener {
            if (checkInfo()) {
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(com.kseb.smart_car.R.id.fcv_join, JoinInfoFragment())
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }
    }

    private fun clickButtonDetail() {
        binding.ivDetail1.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(com.kseb.smart_car.R.id.fcv_join, AgreeServiceFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        binding.ivDetail2.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(com.kseb.smart_car.R.id.fcv_join, AgreeInfoFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        binding.ivDetail3.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(com.kseb.smart_car.R.id.fcv_join, AgreeTransferFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        binding.ivDetail4.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(com.kseb.smart_car.R.id.fcv_join, AgreeLocationFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    private fun checkInfo(): Boolean {
        var isValid = true
        if (!binding.btnAgree1.isSelected) {
            binding.agree1.background =
                ContextCompat.getDrawable(requireContext(), com.kseb.smart_car.R.drawable.border_red)
            isValid = false
        }
        if (!binding.btnAgree2.isSelected) {
            binding.agree2.background =
                ContextCompat.getDrawable(requireContext(), com.kseb.smart_car.R.drawable.border_red)
            isValid = false
        }
        if (!binding.btnAgree3.isSelected) {
            binding.agree3.background =
                ContextCompat.getDrawable(requireContext(), com.kseb.smart_car.R.drawable.border_red)
            isValid = false
        }
        if (!binding.btnAgree4.isSelected) {
            binding.agree4.background =
                ContextCompat.getDrawable(requireContext(), com.kseb.smart_car.R.drawable.border_red)
            isValid = false
        }
        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}