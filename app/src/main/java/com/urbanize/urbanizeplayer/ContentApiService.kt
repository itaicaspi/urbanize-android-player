package com.urbanize.urbanizeplayer

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "https://urbanize-24ffc.firebaseio.com/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface ContentApiService {
    @GET("campaigns/-L_nVNhCiSpTZPO482EC.json?auth=eyJhbGciOiJSUzI1NiIsImtpZCI6Ijc0MWRlNGY0NTMzNzg5YmRiMjUxYjdhNTgwNTZjNTZmY2VkMjE0MWIiLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoiSXRhaSBDYXNwaSIsInBpY3R1cmUiOiJodHRwczovL2ZpcmViYXNlc3RvcmFnZS5nb29nbGVhcGlzLmNvbS92MC9iL3VyYmFuaXplLTI0ZmZjLmFwcHNwb3QuY29tL28vaW1hZ2VzJTJGR09jR2RqYUVSeFdvMHBNNGdZbXpmT05nUHZJMz9hbHQ9bWVkaWEmdG9rZW49ZWRlNTA5NDItYmY5ZC00YTdlLThiYWUtMzc3NmJmYTc3YzljIiwiaXNzIjoiaHR0cHM6Ly9zZWN1cmV0b2tlbi5nb29nbGUuY29tL3VyYmFuaXplLTI0ZmZjIiwiYXVkIjoidXJiYW5pemUtMjRmZmMiLCJhdXRoX3RpbWUiOjE1NjI3MDI0MjAsInVzZXJfaWQiOiJHT2NHZGphRVJ4V28wcE00Z1ltemZPTmdQdkkzIiwic3ViIjoiR09jR2RqYUVSeFdvMHBNNGdZbXpmT05nUHZJMyIsImlhdCI6MTU2MjcwMjQyMCwiZXhwIjoxNTYyNzA2MDIwLCJlbWFpbCI6Iml0YWlAdXJiYW5pemUuY28iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJlbWFpbCI6WyJpdGFpQHVyYmFuaXplLmNvIl19LCJzaWduX2luX3Byb3ZpZGVyIjoicGFzc3dvcmQifX0.ied_PeDEEhIuBjG4-e3KMSeC86o9n0lM2aoNQuBpwyfzPdiaKA0iOC9Tr3BII_EZwX3kjjwjR-TY-l83q23IOVBI_Xmdv1AXNfDEDpDkd50J7KbkfOEEg1ZnN9aYWDeVFnxUqqMPF1guP6MsvodH77QJHgSUl18n0VXKfyMxTNJyYdCYMGZuSkIRH1a-OnGt6qZO-Qdi-g3Q8Ybb2-ZzpV7VDqDfuqoY3zWqiDlX7i93jl2F9wyo502asNloW9PN8-zeTTiamM42-kHnydJaZBADlv_0VAEoPTp0-hBTERYISERgaNAq3DP_aNZy4Q235ywQAMl3XfMJZ175a8xMDA")
    fun getProperties(): Call<List<ContentProperty>>
}

object ContentApi {
    val retrofitService : ContentApiService by lazy {
        retrofit.create(ContentApiService::class.java)
    }
}