package com.urbanize.urbanizeplayer

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.MimeTypeMap
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.urbanize.urbanizeplayer.database.PlayerDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var viewModel: MainViewModel

    private inner class JsObject {
        @JavascriptInterface
        fun videoEnded() {
            // get the next campaign content and preload it
            viewModel.nextCampaign()
            val campaignToLoad = viewModel.nextCampaignToPreload()
            Log.d("campaign to load", campaignToLoad?.pathOnDisk.toString())
            runOnUiThread {
                // wait for 1 seconds for the video to be hidden and then load the next content
                mainWebView.evaluateJavascript("window.setTimeout(function() { loadContent('${campaignToLoad?.pathOnDisk}') }, 1000)", null)
            }
        }
    }

    private lateinit var mainWebView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // hide the status bar and action bar
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        // create the view model
        val dataSource = PlayerDatabase.getInstance(application).playerDatabaseDao
        val viewModelFactory = MainViewModelFactory(dataSource, application)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)

        // initialize the webview player
        mainWebView = startWebPlayer()

        // start backend updates once we have an authentication token
        viewModel.authToken.observeOnce(this, Observer {
            viewModel.startupDeviceUpdates()
        })

        // update the UI with the fetched campaigns
        viewModel.campaigns.observeOnce(this, Observer {newCampaigns ->
            Log.d(TAG, "update campaigns")
            Log.d(TAG, newCampaigns.toString())

//            Toast.makeText(this, newCampaigns.toString(), Toast.LENGTH_LONG).show()

            Handler().postDelayed({
                // play first content
                mainWebView.evaluateJavascript("loadContent('${newCampaigns[0].pathOnDisk}')", null)
                mainWebView.evaluateJavascript("swapContent()", null)

                // preload one content file
                mainWebView.evaluateJavascript("loadContent('${viewModel.nextCampaignToPreload()?.pathOnDisk}')", null)
            }, 2000)

        })
    }

    private fun startWebPlayer(): WebView {
        // get the webview and load the video html5 template
        val mainWebView: WebView = findViewById(R.id.webview)
        mainWebView.webChromeClient = WebChromeClient()
        mainWebView.settings.javaScriptEnabled = true
        mainWebView.settings.mediaPlaybackRequiresUserGesture = false
        mainWebView.settings.setAppCacheEnabled(true)
        mainWebView.settings.domStorageEnabled = true
        mainWebView.settings.databaseEnabled = true

        mainWebView.addJavascriptInterface(JsObject(), "injectedObject")
//        mainWebView.loadUrl("javascript:alert(injectedObject.toString())")

//        mainWebView.loadUrl("http://10.42.0.1:5000/dynamic_content")
        mainWebView.loadUrl("file:///android_asset/dynamic_content_with_tickers.html")

        return mainWebView
    }

}
