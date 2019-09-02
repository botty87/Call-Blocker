package com.call.blocker.settingsActivity

import androidx.lifecycle.MutableLiveData
import com.call.blocker.data.SettingsContainer
import com.call.blocker.data.SettingsContainer.ApplyTo.*
import com.call.blocker.tools.logException

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
                    else -> logException(Exception("Sms filter wrong selection"))
                }
            }
            else {
                when(SettingsContainer.applyTo) {
                    BOTH -> SettingsContainer.applyTo = CALL
                    SMS -> SettingsContainer.applyTo = NONE
                    else -> logException(Exception("Sms filter wrong selection"))
                }
            }
        }
    }

}