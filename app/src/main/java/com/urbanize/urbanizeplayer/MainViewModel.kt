package com.urbanize.urbanizeplayer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.concurrent.fixedRateTimer

class MainViewModel : ViewModel() {

    private val mainRepository = MainRepository()

    val authToken: LiveData<AuthProperty> = mainRepository.getAuthToken()
    var campaigns: MutableLiveData<Map<String, ContentProperty>> = mainRepository.getCampaigns(authToken, 60)

    private val TAG = "MainViewModel"

    init {
        Log.i(TAG, "MainViewModel Created")
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "MainViewModel Destroyed")
    }
}