package com.example.pagaapp.data.network

import com.example.pagaapp.data.model.CountryResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface CountryApiService {
    @GET("v3.1/name/colombia?fields=capital,currencies")
    suspend fun getColombiaInfo(): List<CountryResponse>
}

object RetrofitClient {
    private const val BASE_URL = "https://restcountries.com/"

    val instance: CountryApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CountryApiService::class.java)
    }
}
