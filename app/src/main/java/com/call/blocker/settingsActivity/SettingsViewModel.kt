package com.call.blocker.settingsActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.call.blocker.R
import com.call.blocker.data.SettingsContainer
import com.chibatching.kotpref.livedata.asLiveData

class SettingsViewModel(): ViewModel() {

    /*@get:Bindable
    var readFromContacts: Boolean
    get() {
        return SettingsContainer.readFromContacts
    }
    set(value) {
        SettingsContainer.readFromContacts = value
        notifyPropertyChanged(BR.readFromContacts)
    }*/

    val readFromContacts = SettingBooleanLiveData(SettingsContainer::readFromContacts, SettingsContainer.readFromContacts)
    val filterStatusForSwitch = SettingFilterStatusLiveData(SettingsContainer.filterMode)
    val notificationEnabled = SettingBooleanLiveData(SettingsContainer::isNotificationEnabled, SettingsContainer.isNotificationEnabled)
    val applyToCall = SettingApplyToCallLiveData()
    val applyToSMS = SettingApplyToSMSLiveData()
}