package com.urbanize.urbanizeplayer

import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.fixedRateTimer


class MainRepository(private val filesDir: File) {
    val TAG = "MainRepository"

    private val contentApiService: ContentApiService = ContentApi.retrofitService
    private val authApiService: AuthApiService = AuthApi.retrofitService

    fun getFileType(path: String): String {
        val url = URL(path)
        val connection = url.openConnection() as HttpsURLConnection
        val mime = connection.getHeaderField("Content-Type")
        return mime
    }

    fun download(link: String, path: String) {
        URL(link).openStream().use { input ->
            FileOutputStream(File(path)).use { output ->
                input.copyTo(output)
            }
        }
    }

    fun getAuthToken(): LiveData<AuthProperty> {
        val authToken = MutableLiveData<AuthProperty>()

        val apiKey = "AIzaSyC341Xx6m5yGZMZJ93xaWmf7JOcVF1e4tc"
        val email = "itai@urbanize.co"
        val password = "!2218Lati"
        fixedRateTimer("timer", false, 0, 30*30*1000) {
            authApiService.getAuthToken(apiKey, email, password).enqueue(object : Callback<AuthProperty> {
                override fun onFailure(call: Call<AuthProperty>, t: Throwable) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onResponse(call: Call<AuthProperty>, response: Response<AuthProperty>) {
                    authToken.value = response.body()
                    Log.d("testAuth", authToken.value.toString())
                }
            })
        }

        return authToken
    }


    fun downloadCampaignsContent(campaigns: Map<String, ContentProperty>) {
        val mime = MimeTypeMap.getSingleton()
        campaigns.forEach {
            val link = it.value.content.img
            val ext = mime.getExtensionFromMimeType(getFileType(link))
            val savePath = "${filesDir.path}/${it.key}.${ext}"
            download(link, savePath)
            Log.d(TAG, "Downloaded content to ${savePath}")
        }
    }

    fun getCampaigns(authToken: LiveData<AuthProperty>, periodInSec: Long): MutableLiveData<Map<String, ContentProperty>> {
        val campaigns = MutableLiveData<Map<String, ContentProperty>>()

        fixedRateTimer("timer", false, 10*1000, periodInSec*1000) {
            contentApiService.getCampaigns(authToken.value?.idToken ?: "")
                .enqueue(object : Callback<Map<String, ContentProperty>> {
                    override fun onResponse(
                        call: Call<Map<String, ContentProperty>>,
                        response: Response<Map<String, ContentProperty>>
                    ) {
                        campaigns.value = response.body()
                        Log.d(TAG, campaigns.value.toString())

                        // download the campaigns content to disk
                        GlobalScope.launch {
                            downloadCampaignsContent(campaigns.value ?: emptyMap())
                        }
                    }

                    // Error case is left out for brevity.
                    override fun onFailure(call: Call<Map<String, ContentProperty>>, t: Throwable) {
                        TODO()
                    }
                })
        }

        return campaigns
    }

}