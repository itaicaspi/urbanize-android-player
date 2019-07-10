package com.urbanize.urbanizeplayer

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.concurrent.fixedRateTimer

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val filesDir = application.applicationContext.filesDir
    private val mainRepository = MainRepository(filesDir)
    val authToken: LiveData<AuthProperty> = mainRepository.getAuthToken()
    var campaigns: MutableLiveData<Map<String, ContentProperty>> = mainRepository.getCampaigns(authToken, 60)

    private val TAG = "MainViewModel"

    init {
        Log.d(TAG, application.applicationContext.filesDir.path)
        Log.i(TAG, "MainViewModel Created")
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "MainViewModel Destroyed")
    }
}