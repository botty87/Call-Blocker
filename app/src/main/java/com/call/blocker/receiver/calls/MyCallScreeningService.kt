package com.call.blocker.receiver.calls

import android.telecom.Call
import android.telecom.CallScreeningService
import com.call.blocker.data.PhoneNumber
import com.call.blocker.data.SettingsContainer
import com.google.firebase.firestore.ListenerRegistration

class MyCallScreeningService: CallScreeningService() {

    private var listeners: List<ListenerRegistration>? = null

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle.schemeSpecificPart
        clearListeners()

        listeners = when(SettingsContainer.filterMode) {
            SettingsContainer.Filter.ALLOW_ALL -> {
                CommonBlockTools.checkAllowEndCall(number,
                    { endCall(callDetails, it) },
                    { allowCall(callDetails) })
            }
            SettingsContainer.Filter.BLOCK_ALL -> CommonBlockTools.checkBlockEndCall(
                number,
                { endCall(callDetails, it) },
                { allowCall(callDetails) })
        }
    }

    private fun allowCall(callDetails: Call.Details) {
        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .setSkipCallLog(false)
            .build()

        respondToCall(callDetails, response)
    }

    private fun endCall(callDetails: Call.Details, phoneNumber: PhoneNumber) {
        val response = CallResponse.Builder()
            .setRejectCall(true)
            .setDisallowCall(true)
            .setSkipCallLog(false)
            .build()

        respondToCall(callDetails, response)

        CommonBlockTools.notifyBlockedCall(this, phoneNumber)
    }

    override fun onDestroy() {
        clearListeners()
        super.onDestroy()
    }

    private fun clearListeners() {
        listeners?.forEach { it.remove() }
        listeners = null
    }
}