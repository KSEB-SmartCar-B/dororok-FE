package com.kseb.smart_car.presentation.join

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kseb.smart_car.R
import com.kseb.smart_car.data.service.SpotifyService.connect
import com.kseb.smart_car.databinding.FragmentJoin2Binding
import com.kseb.smart_car.extension.SignUpState
import com.kseb.smart_car.presentation.main.LocationActivity
import com.kseb.smart_car.presentation.main.MainActivity
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.sign

class Join2Fragment: Fragment() {
    private var _binding: FragmentJoin2Binding? = null
    private val binding: FragmentJoin2Binding
        get() = requireNotNull(_binding) { "null" }

    private val viewmodel:JoinViewModel by activityViewModels()
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
            viewmodel.makeSignUp()
            lifecycleScope.launch {
                viewmodel.signUpState.collect{signUpState ->
                    when(signUpState){
                        is SignUpState.Success -> {
                            connect(requireContext()) {
                                if(it){
                                    startActivity(Intent(requireContext(), LocationActivity::class.java).putExtra("accessToken","Bearer ${signUpState.accessToken}"))
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
                        is SignUpState.Loading->{

                        }
                        is SignUpState.Error ->{
                            Log.e("join2Fragment","회원가입 실패 ${signUpState.message}")
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}