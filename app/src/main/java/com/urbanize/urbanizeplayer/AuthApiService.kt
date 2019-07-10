package com.urbanize.urbanizeplayer

import com.google.firebase.database.IgnoreExtraProperties
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*


@IgnoreExtraProperties
data class AuthProperty (
    val idToken: String
)


private const val BASE_URL = "https://www.googleapis.com/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface AuthApiService {
    @POST("/identitytoolkit/v3/relyingparty/verifyPassword")
    @FormUrlEncoded
    fun getAuthToken(@Query("key") apiKey: String,
                     @Field("email") email: String,
                     @Field("password") password: String,
                     @Field("returnSecureToken") returnSecureToken: Boolean = true): Call<AuthProperty>
}

object AuthApi {
    val retrofitService : AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
}