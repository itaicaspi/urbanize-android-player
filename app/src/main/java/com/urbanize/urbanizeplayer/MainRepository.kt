package com.urbanize.urbanizeplayer

import android.app.Application
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.urbanize.urbanizeplayer.database.Campaign
import com.urbanize.urbanizeplayer.database.PlayerDatabaseDao
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.fixedRateTimer


class MainRepository(private val dataSource: PlayerDatabaseDao, private val application: Application) {
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
                    Log.d(TAG, "Failed to fetch auth token. Please check the internet connection.")
                    Log.d(TAG, t.message?:"")
                }

                override fun onResponse(call: Call<AuthProperty>, response: Response<AuthProperty>) {
                    authToken.value = response.body()
                    Log.d("testAuth", authToken.value.toString())
                }
            })
        }

        return authToken
    }


    fun maybeDownloadCampaignsContent(fetchedCampaigns: Map<String, ContentProperty>) {
        val mime = MimeTypeMap.getSingleton()

        // remove campaigns which are not relevant anymore
        val allCampaigns = dataSource.getAllCampaigns()
        Log.d(TAG, "Current campaigns are: $allCampaigns")
        allCampaigns?.forEach {
            val campaignId = it.id
            if (!fetchedCampaigns.containsKey(campaignId)) {
                Log.d(TAG, "Removing campaign $campaignId")

                // remove from DB
                dataSource.removeCampaign(campaignId)

                // remove from internal storage
                application.deleteFile(it.pathOnDisk)
            }
        }

        // update existing campaigns
        fetchedCampaigns.forEach {
            val campaignId = it.key
            val contentUrl = it.value.content.img

            val campaign = dataSource.getCampaign(campaignId)
            Log.d(TAG, "Campaign has current value $campaign")
            if (campaign == null || campaign.originalFilename != contentUrl) {
                Log.d(TAG, "Updating campaign with current value $campaign")

                // download the file
                val ext = mime.getExtensionFromMimeType(getFileType(contentUrl))
                val dir = application.applicationContext.filesDir.path
                val savePath = "$dir/$campaignId.$ext"
                download(contentUrl, savePath)

                Log.d(TAG, "Downloaded content to $savePath")

                // remove old content file if necessary
                if (campaign != null && campaign.pathOnDisk != savePath) {
                    application.deleteFile(campaign.pathOnDisk)
                    Log.d(TAG, "Removed old content from ${campaign.pathOnDisk}")
                }

                // update the database
                val newCampaign = Campaign(campaignId, contentUrl, savePath)
                if (campaign == null) {
                    // this is a new campaign
                    dataSource.insertCampaign(newCampaign)
                } else {
                    // this is an existing campaign which content was updated
                    dataSource.updateCampaign(newCampaign)
                }
            }
        }
    }


    fun getCampaigns(authToken: LiveData<AuthProperty>, periodInSec: Long): MutableLiveData<List<Campaign>> {
        val campaigns = MutableLiveData<List<Campaign>>()

        fixedRateTimer("timer", false, 10*1000, periodInSec*1000) {
            contentApiService.getCampaigns(authToken.value?.idToken ?: "")
                .enqueue(object : Callback<Map<String, ContentProperty>> {
                    override fun onResponse(
                        call: Call<Map<String, ContentProperty>>,
                        response: Response<Map<String, ContentProperty>>
                    ) {
                        val rawCampaigns = response.body()
                        Log.d(TAG, rawCampaigns.toString())

                        // download the campaigns content to disk
                        GlobalScope.launch {
                            campaigns.postValue(dataSource.getAllCampaigns()) // TODO: do this again after updating the DB
                            maybeDownloadCampaignsContent(rawCampaigns ?: emptyMap())
                        }
                    }

                    // Error case is left out for brevity.
                    override fun onFailure(call: Call<Map<String, ContentProperty>>, t: Throwable) {
                        Log.d(TAG, "Failed to fetch campaign. It is possible that the authToken was not fetched correctly")
                        Log.d(TAG, t.message?:"")
                    }
                })
        }

        return campaigns
    }

}