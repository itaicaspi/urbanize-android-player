package com.urbanize.urbanizeplayer

import android.util.JsonReader
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream
import java.io.StringReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.fixedRateTimer

class MainRepository {
    val TAG = "MainRepository"

    private val contentApiService: ContentApiService = ContentApi.retrofitService
    private val authApiService: AuthApiService = AuthApi.retrofitService

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

    fun getAuthToken(): LiveData<AuthProperty> {
        val authToken = MutableLiveData<AuthProperty>()

        val apiKey = "AIzaSyC341Xx6m5yGZMZJ93xaWmf7JOcVF1e4tc"
        val email = "itai@urbanize.co"
        val password = "!2218Lati"
        authApiService.getAuthToken(apiKey, email, password).enqueue(object : Callback<AuthProperty> {
            override fun onFailure(call: Call<AuthProperty>, t: Throwable) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onResponse(call: Call<AuthProperty>, response: Response<AuthProperty>) {
                authToken.value = response.body()
                Log.d("testAuth", authToken.value.toString())
            }

        })

        return authToken
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
                        Log.d("testCampaigns", campaigns.value.toString())
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