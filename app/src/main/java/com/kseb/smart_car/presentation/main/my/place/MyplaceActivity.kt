package com.kseb.smart_car.presentation.main.my.place

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.kseb.smart_car.R
import com.kseb.smart_car.data.responseDto.ResponseFavoritePlaceDto
import com.kseb.smart_car.data.responseDto.ResponseRecommendPlaceNearbyDto
import com.kseb.smart_car.databinding.ActivityMyplaceBinding
import com.kseb.smart_car.presentation.main.place.PlaceViewModel
import com.kseb.smart_car.presentation.main.place.placeDetail.PlaceDetailActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyplaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyplaceBinding
    private val savedPlaceViewModel: SavedPlaceViewModel by viewModels()
    private val placeViewModel: PlaceViewModel by viewModels()
    private lateinit var savedPlaceAdapter: SavedPlaceAdapter
    private var accesstoken:String?=null

    private var editClick: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyplaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        savedPlaceViewModel.setToken(intent.getStringExtra("accessToken")!!)
        setting()
        //replaceFragment(SavedPlaceFragment())
    }

    private fun setting() {
        savedPlaceAdapter = SavedPlaceAdapter(
            { place, ivPhoto -> showDetail(place,ivPhoto) },
            { deletePlace -> deletePlace(deletePlace) }
        )
        binding.rvPlace.adapter = savedPlaceAdapter

        // 데이터를 관찰하는 observer 설정
        placeViewModel.savedPlaceList.observe(this@MyplaceActivity) { list ->
            if (list != null) {
                savedPlaceAdapter.setList(list)
                Log.d("savedPlaceFragment", "list: $list")
            } else {
                Log.d("savedPlaceFragment", "list is null")
            }
        }

        // accessToken 관찰하여 저장된 장소 리스트 요청
        savedPlaceViewModel.accessToken.observe(this@MyplaceActivity) { token ->
            if (token != null) {
                Log.d("SavedplaceFragment", "accessToken observed: $token")
                placeViewModel.setAccessToken(token)
                placeViewModel.accessToken.observe(this@MyplaceActivity) {
                    placeViewModel.getSavedPlaceList()
                    accesstoken=token
                }
            } else {
                Log.d("SavedplaceFragment", "accessToken is null")
            }
        }

        clickButton()
    }

    private fun showDetail(place: ResponseFavoritePlaceDto.FavoritesPlaceListDto, ivPhoto: View) {
        val detailPlace = ResponseRecommendPlaceNearbyDto.PlaceList(place.contentId, place.address, "null", "null", "null", place.imageUrl, place.title)
        val intent = Intent(this, PlaceDetailActivity::class.java).apply {
            putExtra("place", detailPlace)
            putExtra("accessToken",accesstoken)
        }

        val options = ActivityOptions.makeSceneTransitionAnimation(
            this,
            android.util.Pair(ivPhoto, ivPhoto.transitionName),
        )

        startActivity(intent, options.toBundle())
    }

    private fun deletePlace(contentId: String) {

    }

    private fun clickButton(){
        binding.btnEdit.setOnClickListener{
            if (editClick) {
                editClick = false
                binding.btnEdit.text = getText(R.string.my_edit)
            } else {
                editClick = true
                binding.btnEdit.text = getText(R.string.my_delete)
            }
            savedPlaceAdapter.toggleEditMode(editClick)
        }
    }
    /*   private fun replaceFragment(fragment: Fragment) {
           supportFragmentManager.beginTransaction()
               .replace(R.id.fcv_place, fragment)
               .commit()
       }*/
}