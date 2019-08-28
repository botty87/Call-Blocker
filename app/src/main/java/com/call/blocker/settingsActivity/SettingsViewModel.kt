package com.call.blocker.settingsActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.call.blocker.R
import com.call.blocker.data.SettingsContainer

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

    val readFromContacts = SettingLiveData(
        SettingsContainer::readFromContacts,
        SettingsContainer.readFromContacts
    )
    val filterStatusForSwitch =
        SettingFilterStatusLiveData(SettingsContainer.filterMode)
    val filterString: LiveData<Int> = Transformations.map(filterStatusForSwitch, ::getFilterStringRes)
    val applyToCall = SettingApplyToCallLiveData()
    val applyToSMS = SettingApplyToSMSLiveData()

    private fun getFilterStringRes(blockAll: Boolean): Int {
        return if(blockAll) R.string.block_all_desc else R.string.allow_all_desc
    }
}