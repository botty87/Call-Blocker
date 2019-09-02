package com.call.blocker.receiver

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telephony.TelephonyManager
import com.android.internal.telephony.ITelephony
import com.call.blocker.data.*
import com.call.blocker.data.SettingsContainer.ApplyTo.NONE
import com.call.blocker.data.SettingsContainer.ApplyTo.SMS
import com.call.blocker.data.SettingsContainer.Filter.ALLOW_ALL
import com.call.blocker.data.SettingsContainer.Filter.BLOCK_ALL
import com.call.blocker.tools.logException
import com.google.firebase.firestore.Source


//Used for Android < 9 devices.
class IncomingCallReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            context?.run {
                ComponentName(this, IncomingCallReceiver::class.java).let { component ->
                    packageManager.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP)
                }
            }
            return
        }

        val number = intent?.extras?.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
        val state = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)
        if(intent == null || intent.action != "android.intent.action.PHONE_STATE" || context == null ||
                number.isNullOrBlank() || state != TelephonyManager.EXTRA_STATE_RINGING) {
            return
        }

        when(SettingsContainer.applyTo) {
            SMS, NONE -> return
            else -> blockCall(number, context)
        }
    }

    private fun blockCall(number: String, context: Context) {
        fun endCall(context: Context, phoneNumber: PhoneNumber) {
            runCatching {
                val telManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val m = telManager::class.java.getDeclaredMethod("getITelephony")
                m.isAccessible = true
                val telService = m.invoke(telManager) as ITelephony
                telService.endCall()
            }.onFailure {
                logException(it)
            }.onSuccess {
                CommonBlockTools.notifyBlockedCall(context, phoneNumber)
            }
        }

        when(SettingsContainer.filterMode) {
            ALLOW_ALL -> {
                getBlockedNumbersQuery().get(Source.CACHE).addOnSuccessListener { snapshot ->
                    val parser = getPhoneNumberParser()
                    snapshot.documents.firstOrNull {docSnapshot ->
                        val phoneNumber = parser.parseSnapshot(docSnapshot)
                        phoneNumber.number == number
                    }?.let { docSnapshot ->
                        endCall(context, parser.parseSnapshot(docSnapshot))
                    }
                }
            }

            BLOCK_ALL -> {
                getAllowedNumbersQuery().get(Source.CACHE).addOnSuccessListener { snapshot ->
                    val parser = getPhoneNumberParser()
                    val phoneNumber = snapshot.documents.firstOrNull {docSnapshot ->
                        val phoneNumber = parser.parseSnapshot(docSnapshot)
                        phoneNumber.number == number
                    }
                    if(phoneNumber == null) {
                        endCall(context, PhoneNumber(number))
                    }
                }
            }
        }
    }
}