package com.botty.callblocker.data.country

import androidx.lifecycle.MutableLiveData
import com.botty.callblocker.data.COUNTRIES_KEY
import com.botty.callblocker.data.userDocument
import com.botty.callblocker.tools.addSnapshotListenerLogException
import com.google.firebase.firestore.ListenerRegistration
import java.util.*

class CountriesLiveData: MutableLiveData<List<Country>>() {

    private var userListener: ListenerRegistration? = null
    var userUpdate = false

    var locales: List<Locale>? = null
    set(value) {
        userListener?.remove()
        field = value
        value?.let { locales ->
            userListener = userDocument.addSnapshotListenerLogException { documentSnapshot ->
                documentSnapshot?.let { snapshot ->
                    if(userUpdate) {
                        userUpdate = false
                    } else {
                        val userCountries = snapshot[COUNTRIES_KEY] as MutableList<String>?
                        List(locales.size) { index ->
                            val code = locales[index].country
                            val selected = userCountries?.contains(code) ?: false
                            val name = locales[index].displayCountry
                            Country(code, name, selected)
                        }.run {
                            postValue(this)
                        }
                    }
                }
            }
        }
    }

    override fun onInactive() {
        super.onInactive()
        userListener?.remove()
        userListener = null
    }
}