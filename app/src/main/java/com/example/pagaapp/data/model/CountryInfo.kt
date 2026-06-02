package com.example.pagaapp.data.model

data class CountryResponse(
    val capital: List<String>,
    val currencies: Map<String, CurrencyInfo>
)

data class CurrencyInfo(
    val name: String,
    val symbol: String
)
