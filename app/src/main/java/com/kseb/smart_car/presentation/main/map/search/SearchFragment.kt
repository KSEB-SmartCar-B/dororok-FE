package com.kseb.smart_car.presentation.main.map.search

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kakao.sdk.common.KakaoSdk.appKey
import com.kakaomobility.knsdk.KNLanguageType
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.common.objects.KNError_Code_C302
import com.kakaomobility.knsdk.ui.view.KNNaviView
import com.kseb.smart_car.BuildConfig
import com.kseb.smart_car.R
import com.kseb.smart_car.databinding.FragmentSearchBinding
import com.kseb.smart_car.extension.AddSearchState
import com.kseb.smart_car.extension.AddressState
import com.kseb.smart_car.extension.DeleteSearchState
import com.kseb.smart_car.extension.GetSearchState
import com.kseb.smart_car.presentation.main.map.navi.LoadingDialogFragment
import com.kseb.smart_car.presentation.main.map.navi.NaviActivity
import com.kseb.smart_car.presentation.main.map.navi.search.SearchAdapter
import com.kseb.smart_car.presentation.main.map.navi.search.SearchListAdapter
import com.kseb.smart_car.presentation.main.map.navi.search.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.properties.Delegates

@AndroidEntryPoint
class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding: FragmentSearchBinding
        get() = requireNotNull(_binding) { "null" }

    private lateinit var searchAdapter: SearchAdapter
    private lateinit var searchListAdapter: SearchListAdapter
    private val searchViewModel: SearchViewModel by activityViewModels()
    private var loadingDialog:LoadingDialogFragment?=null

    private lateinit var knNaviView: KNNaviView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_CODE_LOCATION_PERMISSION = 123

    private var currentLongitude by Delegates.notNull<Double>()
    private var currentLatitude by Delegates.notNull<Double>()

    private lateinit var speechRecognizer: SpeechRecognizer
    private var textToSpeech: TextToSpeech?=null
    var isTTSReady = false // TTS 준비 상태 플래그


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
                        showLoadingActivity()
                        setKakaoNavi(it.x,it.y, button.placeName)
                    }
                }}

        binding.rvSearch.adapter = searchAdapter
        binding.rvSearchList.adapter=searchListAdapter

        // 검색화면으로 넘어갔을 때 키보드 바로 보이게 함
        binding.rvSearch.requestFocus()
        // 삭제될 때 기본 애니메이션 효과 들어가길래 비활성화
        binding.rvSearch.itemAnimator = null
        binding.btnSearch.isSelected=false

        //권한 설정
        //requestPermission()
        //tts 객체 초기화
        //resetTTS()

        setAccesstoken()
    }

    private fun setAccesstoken() {
        lifecycleScope.launch {
            searchViewModel.accessToken.observe(viewLifecycleOwner) {
                Log.d("searchFragment","accessToken 가져옴!")
                initSearchView()
                //setListener()
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
                        clickButton()
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
                    getSearchList(query)
                    //addSearch(query)
                    //binding.svSearch.setQuery("", false)
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
                    getSearchList(newText)
                }
                return false
            }
        })
    }

    private fun getSearchList(newText:String){
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

    private fun clickButton(){
        // 직접 만든 검색 버튼 활성화
        binding.btnSearch.setOnClickListener {
            binding.btnSearch.isSelected=true
            requestPermission() // 권한 요청 추가
            //tts 객체 초기화
            resetTTS()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(recognitionListener)
                // RecognizerIntent 생성
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_CALLING_PACKAGE,
                        requireActivity().packageName
                    ) //여분의 키
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
                }
                // 여기에 startListening 호출 추가
                startListening(intent)
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

    // Api 호출이 시작되면 LoadingDialogFragment를 보여준다.
    private fun showLoadingActivity() {
        loadingDialog = LoadingDialogFragment("navi")
        loadingDialog?.show(parentFragmentManager, "LoadingDialog")
    }

    // 데이터 로딩이 완료 되면 LoadingDialogFragment를 dismiss 한다.
    private fun closeLoadingActivity() {
        loadingDialog?.dismiss()
    }

    private fun setKakaoNavi(x: Double, y: Double, placeName: String) {
        knNaviView = KNNaviView(requireActivity())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        checkLocation(x,y, placeName)
    }

    private fun initializeKNSDK(x: Double,y: Double, placeName: String) {
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
                    startNavi(x,y, placeName)
                }
            })
    }

    private fun checkLocation(x: Double, y: Double, placeName: String) {
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
                requestLocationPermission(x,y,placeName)
                return
            }

            // 위치 요청
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    // 위치 정보를 가져온 후 처리
                    currentLongitude = location.longitude
                    currentLatitude = location.latitude
                    Log.d("goal", "long:${x}, lati:${y}")
                    initializeKNSDK(x,y, placeName)
                } else {
                    Log.e("searchFragment", "Location is null")
                }
            }
        } else {
            // 위치 권한 요청
            requestLocationPermission(x,y,placeName)
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission(x: Double,y: Double, placeName: String) {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_CODE_LOCATION_PERMISSION
        )
        checkLocation(x, y, placeName)
    }

    private fun startNavi(x: Double, y: Double, placeName: String){
        val intent=Intent(requireActivity(), NaviActivity::class.java)
        intent.putExtra("currentLongitude",currentLongitude)
        intent.putExtra("currentLatitude",currentLatitude)
        intent.putExtra("goalLongitude",x)
        intent.putExtra("goalLatitude",y)
        intent.putExtra("placeName",placeName)
        Log.d("searchFragment","longitude: ${x}, latitude: ${y}")
        startActivity(intent)
    }

    private fun requestPermission() {
        // 버전 체크, 권한 허용했는지 체크
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO), 0
            )
        }
    }

    private fun resetTTS(){
        // TTS 객체 초기화
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.KOREAN)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // 언어 데이터가 없거나 지원하지 않는 언어일 때 처리
                } else {
                    isTTSReady = true // TTS가 준비되었음을 표시
                    //speakInitialMessage() // 초기 메시지 음성 출력
                }
            } else {
                // TTS 초기화 실패 처리
            }
        }
    }

    private fun speakInitialMessage() {
        if(isTTSReady) {
            // 예제 메시지를 TTS로 말하기
            textToSpeech?.speak(requireContext().getString(R.string.search_start), TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

    private val recognitionListener: RecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle) {
            Toast.makeText(context, "이제 말씀하세요!", Toast.LENGTH_SHORT).show()
            //binding.tvState.text = "이제 말씀하세요!"
        }

        override fun onBeginningOfSpeech() {
            //binding.tvState.text = "잘 듣고 있어요."
            Log.d("searchfragment","onBeginningOfSpeech - 잘 듣고 있어요.")
        }

        // 입력받는 소리의 크기를 알려줌
        override fun onRmsChanged(rmsdB: Float) {}

        // 말을 시작하고 인식이 된 단어를 buffer에 담음
        override fun onBufferReceived(buffer: ByteArray) {}

        // 말하기를 중지하면 호출
        override fun onEndOfSpeech() {
            //binding.tvState.text = "끝!"
            Log.d("searchFragment","onendOfSpeech - 끝!")
            CoroutineScope(Dispatchers.Main).launch {
                delay(500)
                //binding.tvState.text="상태체크"
            }
        }

        // 오류 발생했을 때 호출
        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "오디오 에러"
                SpeechRecognizer.ERROR_CLIENT -> "클라이언트 에러"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "퍼미션 없음"
                SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트워크 타임아웃"
                SpeechRecognizer.ERROR_NO_MATCH -> "찾을 수 없음"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RECOGNIZER 가 바쁨"
                SpeechRecognizer.ERROR_SERVER -> "서버가 이상함"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "말하는 시간초과"
                else -> "알 수 없는 오류임"
            }
            // binding.tvState.text = "에러 발생: $message"
            Log.e("searchFragment","onError - error: ${message}")
        }

        override fun onResults(results: Bundle) {
            val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            binding.btnSearch.isSelected=false
            if (!matches.isNullOrEmpty()) {
                val text = matches[0] // 첫 번째 인식 결과를 사용
                if (!text.isNullOrEmpty()) {
                    //addSearch(text)
                    if (binding.svSearch != null) {
                        binding.svSearch.setQuery(text, false)
                        binding.svSearch.clearFocus() // 포커스를 제거하여 불필요한 submit을 방지
                        //addSearch(text)
                    } else {
                        Log.e("searchFragment", "svSearch is null")
                    }
                    Log.d("searchFragment", "Query set to: ${binding.svSearch.query}")
                }
                //messages.add(Message(text,MessageType.USER_INPUT)) // 인식된 텍스트를 messages 리스트에 추가
                //addChatItem(text, MessageType.USER_INPUT)
                // 추가: messages 리스트의 내용을 로그나 UI에 표시하려면 여기에 코드를 추가하세요.
                // 예를 들어, 로그를 사용하여 추가된 메시지를 확인할 수 있습니다.
                Log.d("searchFragment", "인식된 메시지: $text")
                // 혹은 인식된 메시지를 UI에 표시하는 등의 작업을 수행할 수도 있습니다.
            }
        }

        // 부분 인식 결과를 사용할 수 있을 때 호출
        override fun onPartialResults(partialResults: Bundle) {}

        // 향후 이벤트를 추가하기 위해 예약
        override fun onEvent(eventType: Int, params: Bundle) {}
    }


    override fun onResume() {
        super.onResume()
        val safeBinding = _binding ?: return // binding이 null이면 함수 종료
        closeLoadingActivity()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        //sst destroy
        if (this::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }

        // TTS 객체 정리
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}