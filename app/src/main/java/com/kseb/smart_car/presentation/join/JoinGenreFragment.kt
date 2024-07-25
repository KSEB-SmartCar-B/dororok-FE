package com.kseb.smart_car.presentation.join

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.kseb.smart_car.R
import com.kseb.smart_car.data.service.SpotifyService.connect
import com.kseb.smart_car.databinding.FragmentJoinGenreBinding
import com.kseb.smart_car.presentation.main.LocationActivity

class JoinGenreFragment: Fragment() {
    private var _binding: FragmentJoinGenreBinding? = null
    private val binding: FragmentJoinGenreBinding
        get() = requireNotNull(_binding) { "null" }

    private val joinviewmodel by viewModels<JoinViewModel>()
    private val joingenreviewmodel by viewModels<JoinGenreViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJoinGenreBinding.inflate(inflater, container, false)
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

        val joinGenreAdapter = JoinGenreAdapter { buttonText -> joinviewmodel.getGenre(buttonText)}
        binding.rvGenre.adapter = joinGenreAdapter

        joinGenreAdapter.getList(joingenreviewmodel.makeList())

        clickButtonJoin()
    }

    private fun clickButtonJoin() {
        binding.btnJoin.setOnClickListener {
            connect(requireContext()) {
                if(it){
                    startActivity(Intent(requireContext(), LocationActivity::class.java))
                    requireActivity().finish()
                }else {
                    Toast.makeText(
                        requireContext(),
                        R.string.spotify_error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}