package com.call.blocker.settingsActivity

import androidx.lifecycle.MutableLiveData
import kotlin.reflect.KMutableProperty0

class SettingLiveData<T>(private val property: KMutableProperty0<T>, default: T? = null): MutableLiveData<T>() {
    private var firstSet = true
    init {
        value = default
    }

    override fun setValue(value: T?) {
        super.setValue(value)
        if(firstSet) {
            firstSet = false
        }
        else {
            value?.let {
                property.set(it)
            }
        }
    }
}