package com.call.blocker.data

import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.enumpref.enumValuePref

object SettingsContainer: KotprefModel() {
    private const val DEFAULT_READ_FROM_CONTACTS = false
    private val DEFAULT_FILTER_MODE = Filter.BLOCK_ALL
    private val DEFAULT_APPLY_TO = ApplyTo.BOTH

    var readFromContacts by booleanPref(DEFAULT_READ_FROM_CONTACTS)
    var filterMode by enumValuePref(DEFAULT_FILTER_MODE)
    var applyTo by enumValuePref(DEFAULT_APPLY_TO)

    fun resetToDefault() {
        readFromContacts =
            DEFAULT_READ_FROM_CONTACTS
        filterMode =
            DEFAULT_FILTER_MODE
        applyTo =
            DEFAULT_APPLY_TO
    }

    enum class Filter {ALLOW_ALL, BLOCK_ALL}
    enum class ApplyTo {BOTH, CALL, SMS, NONE}
}