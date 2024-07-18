package com.kseb.smart_car.presentation.join

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.FragmentJoin2Binding
import com.kseb.smart_car.presentation.main.LocationActivity
import com.kseb.smart_car.presentation.main.MainActivity

class Join2Fragment: Fragment() {
    private var _binding: FragmentJoin2Binding? = null
    private val binding: FragmentJoin2Binding
        get() = requireNotNull(_binding) { "null" }

    private val viewmodel by viewModels<Join2ViewModel>()

    private lateinit var user: Join
    private var userGenre = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJoin2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvGenre.layoutManager = GridLayoutManager(requireContext(), 3)

        val join2Adapter = Join2Adapter()
        binding.rvGenre.adapter = join2Adapter

        join2Adapter.getList(viewmodel.makeList())

        view.findViewById<View>(R.id.btn_join).setOnClickListener {
            startActivity(Intent(requireContext(), LocationActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}