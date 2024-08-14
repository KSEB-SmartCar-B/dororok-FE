package com.kseb.smart_car.presentation

import androidx.appcompat.app.AppCompatActivity
import com.spotify.android.appremote.api.SpotifyAppRemote

open class BaseActivity : AppCompatActivity() {
    var spotifyAppRemote: SpotifyAppRemote? = null
}