package com.kseb.smart_car.presentation.main.my.place

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.FragmentSavedplaceBinding

class SavedplaceFragment : Fragment() {
    private var _binding: FragmentSavedplaceBinding? = null
    private val binding: FragmentSavedplaceBinding
        get() = requireNotNull(_binding) { "null" }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedplaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val savedplaceAdapter = SavedplaceAdapter()
        binding.rvPlace.adapter = savedplaceAdapter

        clickEditButton()
    }

    private fun clickEditButton() {
        binding.btnEdit.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fcv_place, DeletedplaceFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}