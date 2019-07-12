package com.urbanize.urbanizeplayer

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.urbanize.urbanizeplayer.database.Campaign
import com.urbanize.urbanizeplayer.database.PlayerDatabaseDao
import com.urbanize.urbanizeplayer.network.AuthProperty

class MainViewModel(dataSource: PlayerDatabaseDao, application: Application) : AndroidViewModel(application) {

//    private val filesDir = application.applicationContext.filesDir
    private val mainRepository = MainRepository(dataSource, application)
    val authToken: LiveData<AuthProperty> = mainRepository.getAuthToken()
    var campaigns: MutableLiveData<List<Campaign>> = mainRepository.getCampaigns(authToken, 60)
    var currentRunningCampaign: Int = 0

    private val TAG = "MainViewModel"

    init {
        Log.d(TAG, application.applicationContext.filesDir.path)
        Log.i(TAG, "MainViewModel Created")
    }

    fun startupDeviceUpdates() {
        mainRepository.sendDeviceIsAlive(authToken, 5)
        mainRepository.updateDeviceStatus("loading", authToken)
    }

    fun nextCampaign() {
        currentRunningCampaign = (currentRunningCampaign + 1) % (campaigns.value?.size ?: 1)
    }

    fun nextCampaignToPreload(): Campaign? {
        val nextCampaignIdx = (currentRunningCampaign + 1) % (campaigns.value?.size ?: 1)
        return campaigns.value?.get(nextCampaignIdx)
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "MainViewModel Destroyed")
    }
}