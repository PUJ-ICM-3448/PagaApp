package com.example.pagaapp.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object DirectionsRetrofitClient {
    private const val BASE_URL = "https://maps.googleapis.com/"

    val instance: DirectionsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DirectionsApiService::class.java)
    }
}
