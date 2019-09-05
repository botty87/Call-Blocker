package com.call.blocker.fragments.countriesFragment

import android.app.Application
import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.*
import com.call.blocker.data.country.CountriesLiveData
import com.call.blocker.data.country.Country
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class CountriesFragmentViewModel : ViewModel() {

    init {
        viewModelScope.launch(Dispatchers.Default) {
            val lang = ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0].language
            Locale.getISOCountries()
                .map { isoCode -> Locale(lang, isoCode) }
                .sortedBy { locale -> locale.displayCountry }
                .run { countries.locales = this }
        }
    }

    val countries by lazy { CountriesLiveData() }
}