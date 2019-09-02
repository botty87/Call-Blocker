package com.call.blocker.receiver

import android.telecom.Call
import android.telecom.CallScreeningService
import com.call.blocker.data.*
import com.google.firebase.firestore.Source

class MyCallScreeningService: CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle.schemeSpecificPart

        when(SettingsContainer.filterMode) {
            SettingsContainer.Filter.ALLOW_ALL -> {
                getBlockedNumbersQuery().get(Source.CACHE).addOnSuccessListener { snapshot ->
                    val parser = getPhoneNumberParser()
                    snapshot.documents.firstOrNull {docSnapshot ->
                        val phoneNumber = parser.parseSnapshot(docSnapshot)
                        phoneNumber.number == number
                    }?.let { docSnapshot ->
                        endCall(callDetails, parser.parseSnapshot(docSnapshot))
                    } ?: allowCall(callDetails)
                }
            }

            SettingsContainer.Filter.BLOCK_ALL -> {
                getAllowedNumbersQuery().get(Source.CACHE).addOnSuccessListener { snapshot ->
                    val parser = getPhoneNumberParser()
                    snapshot.documents.firstOrNull {docSnapshot ->
                        val phoneNumber = parser.parseSnapshot(docSnapshot)
                        phoneNumber.number == number
                    }?.run {
                        allowCall(callDetails)
                    } ?: endCall(callDetails, PhoneNumber(number))
                }
            }
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
}