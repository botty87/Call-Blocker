package com.call.blocker.settingsActivity

import androidx.lifecycle.MutableLiveData
import com.call.blocker.data.SettingsContainer
import com.call.blocker.data.SettingsContainer.Filter

class SettingFilterStatusLiveData(filterMode: Filter): MutableLiveData<Boolean>() {
    init {
        value = when(filterMode) {
            Filter.ALLOW_ALL -> true
            Filter.BLOCK_ALL -> false
        }
    }

    override fun setValue(value: Boolean?) {
        super.setValue(value)
        SettingsContainer.filterMode = if(value!!) Filter.ALLOW_ALL else Filter.BLOCK_ALL
    }
}