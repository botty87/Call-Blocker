package com.botty.callblocker.data

import androidx.lifecycle.LifecycleOwner
import com.botty.callblocker.tools.observe
import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.enumpref.enumValuePref
import com.chibatching.kotpref.livedata.asLiveData
import kotlin.reflect.KProperty0

object SettingsContainer: KotprefModel() {
    private const val DEFAULT_READ_FROM_CONTACTS = false
    private const val DEFAULT_NOTIFICATION_ENABLED = false
    private val DEFAULT_FILTER_MODE = Filter.ALLOW_ALL
    private val DEFAULT_APPLY_TO = ApplyTo.BOTH

    var readFromContacts by booleanPref(DEFAULT_READ_FROM_CONTACTS)
    var filterMode by enumValuePref(DEFAULT_FILTER_MODE)
    var applyTo by enumValuePref(DEFAULT_APPLY_TO)
    var isNotificationEnabled by booleanPref(DEFAULT_NOTIFICATION_ENABLED)

    fun resetToDefault() {
        readFromContacts = DEFAULT_READ_FROM_CONTACTS
        filterMode = DEFAULT_FILTER_MODE
        applyTo = DEFAULT_APPLY_TO
        isNotificationEnabled = DEFAULT_NOTIFICATION_ENABLED
    }

    enum class Filter {ALLOW_ALL, BLOCK_ALL}
    enum class ApplyTo {BOTH, CALL, SMS, NONE}
}

fun <T> KotprefModel.observeProperty(property: KProperty0<T>, owner: LifecycleOwner, action: ((T) -> Unit)) {
    this.asLiveData(property).observe(owner, action)
}