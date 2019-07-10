package com.urbanize.urbanizeplayer

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // hide the status bar and action bar
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        // update the UI with the fetched campaigns
        viewModel.campaigns.observe(this, Observer {newCampaigns ->
            Log.d(TAG, newCampaigns.toString())
            Toast.makeText(this, newCampaigns.toString(), Toast.LENGTH_LONG).show()
        })

        // initialize the webview player
        startWebPlayer()
    }

    private fun startWebPlayer() {
        // get the webview and load the video html5 template
        val mainWebView: WebView = findViewById(R.id.webview)
        mainWebView.webChromeClient = WebChromeClient()
        mainWebView.settings.javaScriptEnabled = true
        mainWebView.settings.mediaPlaybackRequiresUserGesture = false
        mainWebView.settings.setAppCacheEnabled(true)
        mainWebView.settings.domStorageEnabled = true
        mainWebView.settings.databaseEnabled = true
        mainWebView.loadUrl("http://10.42.0.1:5000/dynamic_content")
//        mainWebView.loadUrl("file:///android_asset/video.html")
    }

}
