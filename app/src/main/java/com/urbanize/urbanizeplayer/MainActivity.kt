package com.urbanize.urbanizeplayer

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
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
        val mainWebView = startWebPlayer()

        // update the UI with the fetched campaigns
        viewModel.campaigns.observe(this, Observer {newCampaigns ->
            Log.d(TAG, "update campaigns")

//            Toast.makeText(this, newCampaigns.toString(), Toast.LENGTH_LONG).show()
            Log.d(TAG, "Switching content")

            // play first content
            mainWebView.evaluateJavascript("loadContent('${newCampaigns[0].pathOnDisk}')", null)
            mainWebView.evaluateJavascript("swapContent()", null)

            // preload one content file
            mainWebView.evaluateJavascript("loadContent('${newCampaigns[1].pathOnDisk}')", null)

            // TODO: get play end in order to load the next file
        })

//        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//        if (permission != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1240)
//            return
//        }


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
//        mainWebView.loadUrl("http://10.42.0.1:5000/dynamic_content")
        mainWebView.loadUrl("file:///android_asset/dynamic_content_with_tickers.html")

        return mainWebView
    }

}
