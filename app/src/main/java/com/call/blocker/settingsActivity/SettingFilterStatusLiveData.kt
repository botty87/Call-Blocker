package com.call.blocker.settingsActivity

import androidx.lifecycle.MutableLiveData
import com.call.blocker.data.SettingsContainer
import com.call.blocker.data.SettingsContainer.Filter

class SettingFilterStatusLiveData(filterMode: Filter): MutableLiveData<Boolean>() {
    init {
        value = when(filterMode) {
            Filter.ALLOW_ALL -> false
            Filter.BLOCK_ALL -> true
        }
    }

    override fun setValue(value: Boolean?) {
        super.setValue(value)
        SettingsContainer.filterMode = if(value!!) Filter.BLOCK_ALL else Filter.ALLOW_ALL
    }
}