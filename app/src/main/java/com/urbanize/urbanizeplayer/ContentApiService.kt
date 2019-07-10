package com.urbanize.urbanizeplayer

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

private const val BASE_URL = "https://urbanize-24ffc.firebaseio.com/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface ContentApiService {
    @GET("campaigns/-L_nVNhCiSpTZPO482EC.json")
    fun getCampaigns(@Query("auth") auth: String): Call<Map<String, ContentProperty>>
}

object ContentApi {
    val retrofitService : ContentApiService by lazy {
        retrofit.create(ContentApiService::class.java)
    }
}