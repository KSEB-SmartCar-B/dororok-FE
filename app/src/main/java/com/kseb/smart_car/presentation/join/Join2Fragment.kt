package com.kseb.smart_car.presentation.join

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.kseb.smart_car.databinding.FragmentJoin2Binding
import com.kseb.smart_car.presentation.main.MainActivity

class Join2Fragment: Fragment() {
    private var _binding: FragmentJoin2Binding? = null
    private val binding: FragmentJoin2Binding
        get() = requireNotNull(_binding) { "null" }

    private val viewmodel by viewModels<JoinViewModel>()
    private val viewmodel2 by viewModels<Join2ViewModel>()

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

//        viewmodel.buttonText.observe(viewLifecycleOwner) { text ->
//            Toast.makeText(
//                context, "$text", Toast.LENGTH_SHORT
//            ).show()
//        }

        val join2Adapter = Join2Adapter {buttonText -> viewmodel.getGenre(buttonText)}
        binding.rvGenre.adapter = join2Adapter

        join2Adapter.getList(viewmodel2.makeList())

        clickButtonJoin()
    }

    private fun clickButtonJoin() {
        binding.btnJoin.setOnClickListener {
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}