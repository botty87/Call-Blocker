package com.botty.callblocker.settingsActivity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.botty.callblocker.data.SettingsContainer

class SettingsViewModel: ViewModel() {
    val readFromContacts = SettingsContainer::readFromContacts.toMutableLiveData()
    val notificationEnabled = SettingsContainer::isNotificationEnabled.toMutableLiveData()
    val filterStatusForSwitch = SettingFilterStatusLiveData(SettingsContainer.filterMode)
    val ringOnMultipleCalls = SettingsContainer::ringOnMultipleCalls.toMutableLiveData()
    val calls = SettingsContainer::calls.toIntToStringMutableLiveData()
    val minutes = SettingsContainer::minutes.toIntToStringMutableLiveData()

    val buttonsEnabled = MutableLiveData<Boolean>(true)
}