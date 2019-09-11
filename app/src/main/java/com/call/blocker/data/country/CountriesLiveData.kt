package com.call.blocker.data.country

import androidx.lifecycle.MutableLiveData
import com.call.blocker.data.userDocument
import com.call.blocker.tools.log
import com.google.firebase.firestore.ListenerRegistration
import java.util.*

class CountriesLiveData: MutableLiveData<List<Country>>() {
    companion object {
        private const val COUNTRIES_KEY = "countries"
    }

    private var userListener: ListenerRegistration? = null

    var locales: List<Locale>? = null
    set(value) {
        userListener?.remove()
        field = value
        value?.let { locales ->
            userListener = userDocument.addSnapshotListener { documentSnapshot, exception ->
                documentSnapshot?.let { snapshot ->
                    val userCountries = snapshot[COUNTRIES_KEY] as MutableList<String>?
                    List(locales.size) { index ->
                        val code =locales[index].country
                        val selected = userCountries?.contains(code) ?: false
                        val name = locales[index].displayCountry
                        Country(code, name, selected)
                    }.run {
                        postValue(this)
                    }
                }
                exception?.log()
            }
        }
    }

    override fun onInactive() {
        super.onInactive()
        userListener?.remove()
        userListener = null
    }
}