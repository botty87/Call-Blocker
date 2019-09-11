package com.call.blocker.receiver.oldCallReceiver

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import com.android.internal.telephony.ITelephony
import com.call.blocker.data.*
import com.call.blocker.data.SettingsContainer.Filter.*
import com.call.blocker.receiver.CommonBlockTools
import com.call.blocker.tools.log
import com.google.firebase.firestore.ListenerRegistration

class BlockService: Service() {
    companion object {
        internal const val NUMBER_KEY = "number"
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private var userListener: ListenerRegistration? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        clearListeners()
        intent?.getStringExtra(NUMBER_KEY)?.let { number ->
            blockNumber(number)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        clearListeners()
        super.onDestroy()
    }

    private fun clearListeners() {
        userListener?.remove()
        userListener = null
    }

    private fun blockNumber(number: String) {
        fun endCall(phoneNumber: PhoneNumber) {
            runCatching {
                if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.P) {
                    val telManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    val m = telManager::class.java.getDeclaredMethod("getITelephony")
                    m.isAccessible = true
                    val telService = m.invoke(telManager) as ITelephony
                    telService.endCall()
                }
                else {
                    val telManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                    telManager.endCall()
                }
            }.onFailure { e ->
                e.log()
            }.onSuccess {
                CommonBlockTools.notifyBlockedCall(this, phoneNumber)
            }
        }

        userListener = when(SettingsContainer.filterMode) {
            ALLOW_ALL -> CommonBlockTools.checkAllowEndCall(number, ::endCall)
            BLOCK_ALL -> CommonBlockTools.checkBlockEndCall(number, ::endCall)
        }
    }
}