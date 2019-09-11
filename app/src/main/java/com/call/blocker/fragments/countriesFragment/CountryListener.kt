package com.call.blocker.fragments.countriesFragment

import com.call.blocker.data.country.Country

interface CountryListener {
    fun onCountrySelected(country: Country)
}