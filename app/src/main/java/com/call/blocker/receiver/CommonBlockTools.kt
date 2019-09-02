package com.call.blocker.receiver

import android.content.Context
import com.call.blocker.R
import com.call.blocker.data.PhoneNumber
import com.call.blocker.data.SettingsContainer
import com.call.blocker.tools.Constants
import es.dmoral.toasty.Toasty
import io.karn.notify.Notify

object CommonBlockTools {

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
                        summaryTitle = { count ->
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