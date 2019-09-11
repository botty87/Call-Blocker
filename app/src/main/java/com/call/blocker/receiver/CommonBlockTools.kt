package com.call.blocker.receiver

import android.content.Context
import com.call.blocker.R
import com.call.blocker.data.*
import com.call.blocker.tools.Constants
import com.call.blocker.tools.log
import com.google.firebase.firestore.ListenerRegistration
import es.dmoral.toasty.Toasty
import io.karn.notify.Notify

object CommonBlockTools {

    fun checkAllowEndCall(number: String, endCallAction: (phoneNumber: PhoneNumber) -> Unit, allowCallAction: (() -> Unit)? = null)
            : ListenerRegistration {
        return findBlockedNumberQuery(number).addSnapshotListener { snapshot, exception ->
            snapshot?.documents?.firstOrNull()?.run {
                val parser = getPhoneNumberParser()
                endCallAction.invoke(parser.parseSnapshot(this))
            } ?: allowCallAction?.invoke()
            exception?.log()
        }
    }

    fun checkBlockEndCall(number: String, endCallAction: (phoneNumber: PhoneNumber) -> Unit, allowCallAction: (() -> Unit)? = null)
            : ListenerRegistration {
        return findAllowedNumberQuery(number).addSnapshotListener { snapshot, exception ->
            if(snapshot?.isEmpty == true) {
                endCallAction.invoke(PhoneNumber(number))
            } else {
              allowCallAction?.invoke()
            }
            exception?.log()
        }
    }

    fun notifyBlockedCall(context: Context, phoneNumber: PhoneNumber) {
        val message = "${context.getString(R.string.call_blocked_from)}: ${phoneNumber.description ?: phoneNumber.number}"
        Toasty.error(context, message, Toasty.LENGTH_LONG).show()

        if(SettingsContainer.isNotificationEnabled) {
            context.run {
                Notify
                    .with(this)
                    .alerting(Constants.NOTIFICATION_CHANNEL_ID) {
                        channelImportance = Notify.IMPORTANCE_LOW
                    }
                    .content {
                        title = getString(R.string.call_blocked)
                        text = message
                    }
                    .stackable {
                        key = "call_not_key"
                        summaryContent = message
                        summaryTitle = {
                            getString(R.string.calls_blocked)
                        }
                        summaryDescription = { count ->
                            "$count ${getString(R.string.calls_blocked).decapitalize()}"
                        }
                    }
                    .show()
            }
        }
    }

}