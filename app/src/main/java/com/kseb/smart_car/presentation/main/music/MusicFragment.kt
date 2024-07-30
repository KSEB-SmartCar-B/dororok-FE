package com.kseb.smart_car.presentation.main.music

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
import com.kseb.smart_car.databinding.FragmentMusicBinding

class MusicFragment: Fragment() {
    private var _binding: FragmentMusicBinding?= null
    private val binding: FragmentMusicBinding
        get() = requireNotNull(_binding) {"null"}

    private val viewmodel by viewModels<SituationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMusicBinding.inflate(inflater, container, false)
        return binding.root }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvSituation.layoutManager = GridLayoutManager(requireContext(), 3)

        val situationAdapter = SituationAdapter (requireContext(), { situation -> onItemClicked(situation) },"music")
        binding.rvSituation.adapter = situationAdapter
        situationAdapter.getList(viewmodel.makeList())
    }

    private fun onItemClicked(situation: String) {
        connect(requireContext()) { isConnected ->
            if (isConnected) {
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fcv_main, PlayFragment())
                transaction.addToBackStack(null)
                transaction.commit()
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.spotify_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

//    private fun clickButton(){
//        with(binding){
//            rvSituation.setOnClickListener{
//                connect(requireContext()) {
//                    if(it){
//                        val transaction = requireActivity().supportFragmentManager.beginTransaction()
//                        transaction.replace(R.id.fcv_main, PlayFragment())
//                        transaction.addToBackStack(null)
//                        transaction.commit()
//                    }else {
//                        Toast.makeText(
//                            requireContext(),
//                            R.string.spotify_error,
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//            }
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null }
}