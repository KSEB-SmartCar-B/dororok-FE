package com.kseb.smart_car.presentation.main.music

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kseb.smart_car.NoScrollRecyclerView
import com.kseb.smart_car.R
import com.kseb.smart_car.data.service.SpotifyService.connect
import com.kseb.smart_car.databinding.FragmentMainBinding

class MainFragment:Fragment() {
    private var _binding:FragmentMainBinding?=null
    private val binding:FragmentMainBinding
        get() = requireNotNull(_binding){"null"}

    private val viewmodel by viewModels<SituationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding=FragmentMainBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val situationAdapter = SituationAdapter (requireContext(), { situation -> onItemClicked(situation) },"music")
        binding.rvSituation.adapter = situationAdapter
        situationAdapter.getList(viewmodel.makeList())

        //리사이클러뷰 스크롤 안되게 (한페이지에 들어올 것 같아서 꿀렁거리는 느낌 빼고싶어서)
        val recyclerView: NoScrollRecyclerView = view.findViewById(R.id.rv_situation)
        recyclerView.itemAnimator = null
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null }
}