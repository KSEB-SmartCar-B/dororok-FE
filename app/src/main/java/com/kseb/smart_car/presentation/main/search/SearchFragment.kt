package com.kseb.smart_car.presentation.main.search

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kakao.sdk.common.KakaoSdk.appKey
import com.kakao.vectormap.LatLng
import com.kakaomobility.knsdk.KNLanguageType
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.common.objects.KNError_Code_C302
import com.kakaomobility.knsdk.ui.view.KNNaviView
import com.kseb.smart_car.BuildConfig
import com.kseb.smart_car.databinding.FragmentSearchBinding
import com.kseb.smart_car.extension.AddSearchState
import com.kseb.smart_car.extension.AddressState
import com.kseb.smart_car.extension.DeleteSearchState
import com.kseb.smart_car.extension.GetSearchState
import com.kseb.smart_car.presentation.main.navi.NaviActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

@AndroidEntryPoint
class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding: FragmentSearchBinding
        get() = requireNotNull(_binding) { "null" }

    private lateinit var searchAdapter: SearchAdapter
    private lateinit var searchListAdapter: SearchListAdapter
    private val searchViewModel: SearchViewModel by activityViewModels()

    private lateinit var knNaviView: KNNaviView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_CODE_LOCATION_PERMISSION = 123

    private var currentLongitude by Delegates.notNull<Double>()
    private var currentLatitude by Delegates.notNull<Double>()

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

        searchAdapter = SearchAdapter ({ buttonText -> deleteSearch(buttonText) }, {button -> binding.svSearch.setQuery(button,false)})
        searchListAdapter =
            SearchListAdapter { button ->
                searchViewModel.addSearch(button.placeName)
            searchViewModel.setGoalCoordinate(button.x.toDouble(),button.y.toDouble())
            lifecycleScope.launch {
                searchViewModel.goalState.observe(viewLifecycleOwner){
                    setKakaoNavi(it.x,it.y)
                }
            }}

        binding.rvSearch.adapter = searchAdapter
        binding.rvSearchList.adapter=searchListAdapter

        // 삭제될 때 기본 애니메이션 효과 들어가길래 비활성화
        binding.rvSearch.itemAnimator = null

        setAccesstoken()
    }

    private fun setAccesstoken() {
        lifecycleScope.launch {
            searchViewModel.accessToken.observe(viewLifecycleOwner) {
                initSearchView()
                setListener()
            }
        }
    }

    //최근 검색 기록 가져오기
    private fun initSearchView() {
        binding.rvSearch.visibility = View.VISIBLE
        binding.rvSearchList.visibility = View.INVISIBLE
        searchViewModel.getSearch()
        lifecycleScope.launch {
            searchViewModel.searchState.collect { searchState ->
                when (searchState) {
                    is GetSearchState.Success -> {
                        searchAdapter.getList(searchState.searchDto)
                        setListener()
                        searchViewModel.setSearchLoading("get")
                        Log.d("searchFragment", "get list success!")
                    }

                    is GetSearchState.Loading -> {
                        Log.d("searchFragment", "get list loading")
                    }

                    is GetSearchState.Error -> {
                        Log.e("searchFragment", "get search state error!")
                    }
                }
            }
        }

    }

    private fun setListener() {
        // SearchView에서 생성되는 타자에 있는 돋보기 버튼 활성화
        binding.svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query.isNotEmpty()) {
                    addSearch(query)
                    binding.svSearch.setQuery("", false)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                 if(newText.isNullOrEmpty()) {
                    Log.d("searchFragment", "text is null")
                    binding.rvSearch.visibility = View.VISIBLE
                    binding.rvSearchList.visibility = View.INVISIBLE
                } else if (newText != null) {
                     Log.d("searchFragment", "text:${newText}")
                     binding.rvSearch.visibility = View.INVISIBLE
                     binding.rvSearchList.visibility = View.VISIBLE
                     searchViewModel.getAddress(newText)
                     lifecycleScope.launch {
                         searchViewModel.addressState.collect { addressState ->
                             when (addressState) {
                                 is AddressState.Success -> {
                                     Log.d("searchFragment", "주소 가져오기 성공!")
                                     if (addressState.addressDto.documents.isNotEmpty())
                                         searchListAdapter.getList(addressState.addressDto.documents)

                                 }

                                 is AddressState.Loading -> {}
                                 is AddressState.Error -> {
                                     Log.e("searchFragment", "주소 가져오기 에러!")
                                 }
                             }
                         }
                     }
                 }
                return false
            }
        })

        // 직접 만든 검색 버튼 활성화
        binding.btnSearch.setOnClickListener {
            val text = binding.svSearch.query.toString()
            if (text.isNotEmpty()) {
                addSearch(text)
                binding.svSearch.setQuery("", false)
            }
        }
    }

    private fun addSearch(searchLog: String) {
        searchViewModel.addSearch(searchLog)
        lifecycleScope.launch {
            searchViewModel.addSearchState.collect { searchState ->
                when (searchState) {
                    is AddSearchState.Success -> {
                        Log.d("searchFragement", "add search success!")
                        initSearchView()
                        searchViewModel.setSearchLoading("add")
                    }

                    is AddSearchState.Loading -> {}
                    is AddSearchState.Error -> {
                        Log.e("searchFragment", "add search state error! ${searchState.message}")
                    }
                }
            }
        }
    }

    private fun deleteSearch(searchLog: String) {
        searchViewModel.deleteSearch(searchLog)
        lifecycleScope.launch {
            searchViewModel.deleteSearchState.collect { searchState ->
                when (searchState) {
                    is DeleteSearchState.Success -> {
                        Log.d("searchFragement", "delete search success!")
                        initSearchView()
                        searchViewModel.setSearchLoading("delete")
                    }

                    is DeleteSearchState.Loading -> {}
                    is DeleteSearchState.Error -> {
                        Log.e("searchFragment", "delete search state error! ${searchState.message}")
                    }
                }
            }
        }
    }

    private fun setKakaoNavi(x:Double,y: Double) {
        knNaviView = KNNaviView(requireActivity())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        checkLocation(x,y)
    }

    private fun initializeKNSDK(x: Double,y: Double) {
        KNSDK.initializeWithAppKey(
            appKey, BuildConfig.VERSION_NAME,
            null, KNLanguageType.KNLanguageType_KOREAN, aCompletion = {
                if (it != null) {
                    when (it.code) {
                        KNError_Code_C302 -> {
                            Log.e("error", "error code c302")
                        }

                        else -> {
                            Log.e("error", "unknown error")
                        }
                    }
                } else {
                    // 인증 완료
                    Log.d("searchFragment", "인증완료")
                    Log.d("searchFragment", "initialize -> 현재좌표: ${currentLongitude}, ${currentLatitude}")
                    startNavi(x,y)
                }
            })
    }

    private fun checkLocation(x: Double,y: Double) {
        // 위치 권한이 이미 허용되어 있는지 확인
        if (isLocationPermissionGranted()) {
            // 위치 기능 사용 가능
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 권한이 없는 경우 처리
                requestLocationPermission(x,y)
                return
            }

            // 위치 요청
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    // 위치 정보를 가져온 후 처리
                    currentLongitude = location.longitude
                    currentLatitude = location.latitude
                    Log.d("goal", "long:${x}, lati:${y}")
                    initializeKNSDK(x,y)
                } else {
                    Log.e("searchFragment", "Location is null")
                }
            }
        } else {
            // 위치 권한 요청
            requestLocationPermission(x,y)
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission(x: Double,y: Double) {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_CODE_LOCATION_PERMISSION
        )
        checkLocation(x,y)
    }

    private fun startNavi(x:Double, y:Double){
        val intent=Intent(requireActivity(),NaviActivity::class.java)
        intent.putExtra("currentLongitude",currentLongitude)
        intent.putExtra("currentLatitude",currentLatitude)
        intent.putExtra("goalLongitude",x)
        intent.putExtra("goalLatitude",y)
        Log.d("searchFragment","longitude: ${x}, latitude: ${y}")
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}