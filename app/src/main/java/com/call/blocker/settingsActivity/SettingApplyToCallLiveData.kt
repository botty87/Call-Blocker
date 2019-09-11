package com.call.blocker.settingsActivity

import androidx.lifecycle.MutableLiveData
import com.call.blocker.data.SettingsContainer
import com.call.blocker.data.SettingsContainer.ApplyTo.*
import com.call.blocker.tools.log

class SettingApplyToCallLiveData: MutableLiveData<Boolean>() {

    init {
        value = when(SettingsContainer.applyTo) {
            BOTH, CALL -> true
            else -> false
        }
    }

    override fun setValue(checked: Boolean?) {
        super.setValue(checked)
        checked?.run {
            if(checked) {
                when(SettingsContainer.applyTo) {
                    NONE -> SettingsContainer.applyTo = CALL
                    SMS -> SettingsContainer.applyTo = BOTH
                    else -> Exception("Call filter wrong selection").log()
                }
            }
            else {
                when(SettingsContainer.applyTo) {
                    BOTH -> SettingsContainer.applyTo = SMS
                    CALL -> SettingsContainer.applyTo = NONE
                    else -> Exception("Call filter wrong selection").log()
                }
            }
        }
    }

}