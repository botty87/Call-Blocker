package com.call.blocker.settingsActivity

import androidx.lifecycle.MutableLiveData
import kotlin.reflect.KMutableProperty0

class SettingBooleanLiveData(private val property: KMutableProperty0<Boolean>, default: Boolean): MutableLiveData<Boolean>() {
    private var firstSet = true
    init {
        value = default
    }

    override fun setValue(value: Boolean?) {
        super.setValue(value)
        if(firstSet) {
            firstSet = false
        }
        else {
            value?.run { property.set(this) }
        }
    }
}