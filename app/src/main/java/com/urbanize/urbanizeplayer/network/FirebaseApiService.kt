package com.urbanize.urbanizeplayer.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val BASE_URL = "https://urbanize-24ffc.firebaseio.com/"
private const val DEVICE_ID = "ehRACOVcj5g4OUYvG2qugkmaTba2"   // TODO: get device id

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface FirebaseApiService {
    @GET("upcoming_campaigns_per_device/$DEVICE_ID.json")
    fun getCampaigns(@Query("auth") auth: String): Call<Map<String, ContentProperty>>

    @POST("status_updates/$DEVICE_ID.json")
    fun sendDeviceIsAlive(@Body status: IsAliveUpdateProperty, @Query("auth") auth: String): Call<Map<String, String>>

    @PATCH("devices/$DEVICE_ID.json")
    fun updateDeviceStatus(@Body status: DeviceStatusProperty, @Query("auth") auth: String): Call<Map<String, String>>

    @GET("info_tickers/$DEVICE_ID.json")
    fun getInfoTicker(@Query("auth") auth: String): Call<Map<String, InfoTickerEntryProperty>>

}

object FirebaseApi {
    val retrofitService : FirebaseApiService by lazy {
        retrofit.create(FirebaseApiService::class.java)
    }
}