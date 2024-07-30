package com.kseb.smart_car.presentation.main.search

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.kseb.smart_car.databinding.FragmentSearchBinding

class SearchFragment: Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding: FragmentSearchBinding
        get() = requireNotNull(_binding) { "null" }

    private lateinit var searchAdapter: SearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchAdapter = SearchAdapter()
        binding.rvSearch.adapter = searchAdapter

        // 삭제될 때 기본 애니메이션 효과 들어가길래 비활성화
        binding.rvSearch.itemAnimator = null

        initSearchView()
        onSearchClicked()

    }

    // SearcgView에서 생성되는 타자에 있는 돋보기 버튼 활성화
    private fun initSearchView() {
        binding.svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query.isNotEmpty()) {
                    searchAdapter.getList(query)
                    binding.svSearch.setQuery("", false)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    // 직접 만든 검색 버튼 활성화
    private fun onSearchClicked() {
        binding.btnSearch.setOnClickListener {
            val text = binding.svSearch.query.toString()

            if (text.isNotEmpty()) {
                searchAdapter.getList(text)
                binding.svSearch.setQuery("", false)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}