package com.botty.callblocker.settingsActivity

import androidx.lifecycle.MutableLiveData
import com.botty.callblocker.data.SettingsContainer
import com.botty.callblocker.data.SettingsContainer.ApplyTo.*
import com.botty.callblocker.tools.log

class SettingApplyToSMSLiveData: MutableLiveData<Boolean>() {

    init {
        value = when(SettingsContainer.applyTo) {
            BOTH, SMS -> true
            else -> false
        }
    }

    override fun setValue(checked: Boolean?) {
        super.setValue(checked)
        checked?.run {
            if(checked) {
                when(SettingsContainer.applyTo) {
                    NONE -> SettingsContainer.applyTo = SMS
                    CALL -> SettingsContainer.applyTo = BOTH
                    else -> Exception("Sms filter wrong selection").log()
                }
            }
            else {
                when(SettingsContainer.applyTo) {
                    BOTH -> SettingsContainer.applyTo = CALL
                    SMS -> SettingsContainer.applyTo = NONE
                    else -> Exception("Sms filter wrong selection").log()
                }
            }
        }
    }

}