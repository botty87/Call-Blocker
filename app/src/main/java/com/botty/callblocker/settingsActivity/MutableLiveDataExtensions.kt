package com.botty.callblocker.settingsActivity

import androidx.lifecycle.MutableLiveData
import kotlin.reflect.KMutableProperty0

private class GenericMutableLiveData<T>(private val property: KMutableProperty0<T>): MutableLiveData<T>() {
    private var firstSet = true
    init {
        value = property.get()
    }

    override fun setValue(value: T?) {
        super.setValue(value)
        if(firstSet) {
            firstSet = false
        }
        else {
            value?.run { property.set(this) }
        }
    }
}

private class IntToStringMutableLiveData(private val property: KMutableProperty0<Int>): MutableLiveData<String>() {
    init {
        value = property.get().toString()
    }

    override fun setValue(value: String?) {
        val intValue = value?.toIntOrNull()
        if(intValue?.run { this <= 0 } != false) {
            super.setValue("")
            property.set(0)
        } else {
            super.setValue(value)
            property.set(intValue)
        }
    }
}

fun <T> KMutableProperty0<T>.toMutableLiveData(): MutableLiveData<T> = GenericMutableLiveData(this)
fun KMutableProperty0<Int>.toIntToStringMutableLiveData(): MutableLiveData<String> = IntToStringMutableLiveData(this)