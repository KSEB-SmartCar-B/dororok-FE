package com.kseb.smart_car.presentation.main.music

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.kseb.smart_car.R
import com.kseb.smart_car.data.service.SpotifyService.connect
import com.kseb.smart_car.databinding.FragmentMusicBinding
import com.kseb.smart_car.databinding.FragmentMyBinding
import com.kseb.smart_car.presentation.join.Join2Fragment
import com.kseb.smart_car.presentation.main.LocationActivity

class MusicFragment: Fragment() {
    private var _binding: FragmentMusicBinding?= null
    private val binding: FragmentMusicBinding
        get() = requireNotNull(_binding) {"null"}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMusicBinding.inflate(inflater, container, false)
        return binding.root }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clickButton()
    }

    private fun clickButton(){
        with(binding){
            ibSituIlsang.setOnClickListener{
                connect(requireContext()) {
                    if(it){
                        val transaction = requireActivity().supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.fcv_main, PlayFragment())
                        transaction.addToBackStack(null)
                        transaction.commit()
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null }
}