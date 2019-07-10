package com.urbanize.urbanizeplayer

import android.util.JsonReader
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.InputStream
import java.io.StringReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MainViewModel : ViewModel() {

    private val _campaigns = MutableLiveData<String>()
    val campaigns : LiveData<String>
        get() = _campaigns

    private val TAG = "MainViewModel"

    private fun getJsonValue(json: String, requestedKey: String) : String {
        val jsonReader = JsonReader(StringReader(json))
        jsonReader.beginObject() // Start processing the JSON object
        while (jsonReader.hasNext()) { // Loop through all keys
            val currentKey = jsonReader.nextName() // Fetch the next key
            if (currentKey == requestedKey) { // Check if desired key
                // Fetch the value as a String
                val requestedValue = jsonReader.nextString()
                jsonReader.close()
                return requestedValue
            } else {
                jsonReader.skipValue() // Skip values of other keys
            }
        }
        jsonReader.close()
        return ""
    }

    private fun getJsonKeys(json: String) : MutableList<String> {
        val jsonReader = JsonReader(StringReader(json))
        jsonReader.beginObject() // Start processing the JSON object
        val keys = mutableListOf<String>()
        while (jsonReader.hasNext()) { // Loop through all keys
            val currentKey = jsonReader.nextName() // Fetch the next key
            keys.add(currentKey)
            jsonReader.skipValue() // Skip values of other keys
        }
        jsonReader.close()
        return keys
    }

    private fun getAuthToken(email: String, password: String): String {
        val apiKey = "AIzaSyC341Xx6m5yGZMZJ93xaWmf7JOcVF1e4tc"
        val firebaseAuthEndpoint = URL("https://www.googleapis.com/identitytoolkit/v3/relyingparty/verifyPassword?key=${apiKey}")

        // Create connection
        val firebaseConnection = firebaseAuthEndpoint.openConnection() as HttpsURLConnection
        firebaseConnection.setRequestProperty("Content-Type", "application/json")
        firebaseConnection.requestMethod = "POST"
        firebaseConnection.doOutput = true  // enable writing

        // Create the authentication data
        val authData = "{\"email\":\"$email\",\"password\":\"$password\",\"returnSecureToken\":true}"

        // Write the authentication data
        firebaseConnection.outputStream.write(authData.toByteArray())

        if (firebaseConnection.responseCode == 200) {
            // Success
            val content = firebaseConnection.inputStream.extractString()
            val authToken = getJsonValue(content, "idToken")
            firebaseConnection.disconnect()
            return authToken
        } else {
            Log.e(TAG, "Error fetching authentication token")
            // Error handling code goes here
            firebaseConnection.disconnect()
            return ""
        }
    }


    private fun InputStream.extractString() : String {
        return this.bufferedReader().use { it.readText() }
    }

    private fun queryDatabase(path: String, authToken: String): String {
        val firebaseRef = URL("https://urbanize-24ffc.firebaseio.com/${path}.json?auth=${authToken}")

        // Create connection
        val firebaseConnection = firebaseRef.openConnection() as HttpsURLConnection
        firebaseConnection.setRequestProperty("Content-Type", "application/json")
        firebaseConnection.requestMethod = "GET"

        if (firebaseConnection.responseCode == 200) {
            // Success
            val content = firebaseConnection.inputStream.extractString()
            firebaseConnection.disconnect()
            Log.d("get_campaigns_internal", content)
            return content
        } else {
            // Error handling code goes here
            Log.e(TAG, "Error querying database")
            firebaseConnection.disconnect()
            return ""
        }
    }

    private val _response = MutableLiveData<String>()

    fun fetchCampaigns() {
        val authToken = getAuthToken("itai@urbanize.co", "!2218Lati")
        _campaigns.postValue(queryDatabase("/campaigns/-L_nVNhCiSpTZPO482EC", authToken))
        Log.d("get_campaigns", _campaigns.value ?: "")
    }


    init {
        Log.i(TAG, "MainViewModel Created")
        _campaigns.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "MainViewModel Destroyed")
    }
}