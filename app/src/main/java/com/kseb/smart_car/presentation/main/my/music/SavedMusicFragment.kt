package com.kseb.smart_car.presentation.main.my.music

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.FragmentSavedmusicBinding
import com.kseb.smart_car.presentation.main.my.place.DeletedplaceFragment
import com.kseb.smart_car.presentation.main.my.place.SavedplaceAdapter

class SavedmusicFragment : Fragment() {
    private var _binding: FragmentSavedmusicBinding? = null
    private val binding: FragmentSavedmusicBinding
        get() = requireNotNull(_binding) { "null" }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedmusicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val savedmusicAdapter = SavedmusicAdapter()
        binding.rvMusic.adapter = savedmusicAdapter

        clickEditButton()
    }

    private fun clickEditButton() {
        binding.btnEdit.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fcv_music, DeletedmusicFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}