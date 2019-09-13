package com.call.blocker.receiver.calls.compatReceiver

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telephony.TelephonyManager
import com.call.blocker.data.SettingsContainer
import com.call.blocker.data.SettingsContainer.ApplyTo.NONE
import com.call.blocker.data.SettingsContainer.ApplyTo.SMS
import com.call.blocker.receiver.calls.compatReceiver.BlockService.Companion.NUMBER_KEY
import org.jetbrains.anko.startService
import org.jetbrains.anko.stopService


//Used for Android < 10 devices.
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
                number.isNullOrBlank() || state == null) {
            return
        }

        when(SettingsContainer.applyTo) {
            SMS, NONE -> return
            else -> blockCallService(number, context, state)
        }
    }

    private fun blockCallService(number: String, context: Context, state: String) {
        when(state) {
            TelephonyManager.EXTRA_STATE_RINGING -> context.startService<BlockService>(NUMBER_KEY to number)
            TelephonyManager.EXTRA_STATE_IDLE -> context.stopService<BlockService>()
        }
    }
}