package com.urbanize.urbanizeplayer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val mainRepository = MainRepository()

    val campaigns: LiveData<String> = mainRepository.getCampaigns()

    private val TAG = "MainViewModel"

    init {
        Log.i(TAG, "MainViewModel Created")
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "MainViewModel Destroyed")
    }
}