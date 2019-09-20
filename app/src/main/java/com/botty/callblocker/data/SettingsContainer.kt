package com.botty.callblocker.data

import androidx.lifecycle.LifecycleOwner
import com.botty.callblocker.tools.observe
import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.enumpref.enumValuePref
import com.chibatching.kotpref.livedata.asLiveData
import kotlin.reflect.KProperty0

object SettingsContainer: KotprefModel() {
    private const val DEFAULT_READ_FROM_CONTACTS = false
    private const val DEFAULT_NOTIFICATION_ENABLED = true
    private val DEFAULT_FILTER_MODE = Filter.ALLOW_ALL
    private const val DEFAULT_MULTIPLE_CALLS_RING = false
    private const val DEFAULT_CALLS_AND_MINUTES = 2

    var readFromContacts by booleanPref(DEFAULT_READ_FROM_CONTACTS)
    var filterMode by enumValuePref(DEFAULT_FILTER_MODE)
    var isNotificationEnabled by booleanPref(DEFAULT_NOTIFICATION_ENABLED)
    var ringOnMultipleCalls by booleanPref(DEFAULT_MULTIPLE_CALLS_RING)
    var calls by intPref(DEFAULT_CALLS_AND_MINUTES)
    var minutes by intPref(DEFAULT_CALLS_AND_MINUTES)

    fun resetToDefault() {
        readFromContacts = DEFAULT_READ_FROM_CONTACTS
        filterMode = DEFAULT_FILTER_MODE
        isNotificationEnabled = DEFAULT_NOTIFICATION_ENABLED
        ringOnMultipleCalls = DEFAULT_MULTIPLE_CALLS_RING
        calls = DEFAULT_CALLS_AND_MINUTES
        minutes = DEFAULT_CALLS_AND_MINUTES
    }

    enum class Filter {ALLOW_ALL, BLOCK_ALL}
}