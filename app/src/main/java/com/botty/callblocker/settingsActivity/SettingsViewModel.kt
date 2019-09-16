package com.botty.callblocker.settingsActivity

import androidx.lifecycle.ViewModel
import com.botty.callblocker.data.SettingsContainer

class SettingsViewModel: ViewModel() {
    val readFromContacts = SettingBooleanLiveData(SettingsContainer::readFromContacts, SettingsContainer.readFromContacts)
    val filterStatusForSwitch = SettingFilterStatusLiveData(SettingsContainer.filterMode)
    val notificationEnabled = SettingBooleanLiveData(SettingsContainer::isNotificationEnabled, SettingsContainer.isNotificationEnabled)
    val applyToCall = SettingApplyToCallLiveData()
    val applyToSMS = SettingApplyToSMSLiveData()
}