package com.botty.callblocker.fragments.countriesFragment

import com.botty.callblocker.data.country.Country

interface CountryListener {
    fun onCountrySelected(country: Country)
}