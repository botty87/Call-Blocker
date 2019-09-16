package com.botty.callblocker.receiver.calls

import android.content.Context
import com.botty.callblocker.R
import com.botty.callblocker.data.*
import com.botty.callblocker.tools.Constants
import com.botty.callblocker.tools.addSnapshotListenerLogException
import com.google.firebase.firestore.ListenerRegistration
import es.dmoral.toasty.Toasty
import io.karn.notify.Notify
import java.util.concurrent.atomic.AtomicInteger

object CommonBlockTools {

    fun checkAllowEndCall(number: String, endCallAction: (phoneNumber: PhoneNumber) -> Unit, allowCallAction: (() -> Unit)? = null)
            : List<ListenerRegistration> {
        val listeners = mutableListOf<ListenerRegistration>()
        findBlockedNumberQuery(number).addSnapshotListenerLogException { snapshot ->
            snapshot?.documents?.firstOrNull()?.run {
                val parser = getPhoneNumberParser()
                endCallAction.invoke(parser.parseSnapshot(this))
            } ?: searchUserLists(number, endCallAction, allowCallAction, listeners)
        }.run { listeners.add(this) }
        return listeners
    }

    private fun searchUserLists(number: String, endCallAction: (phoneNumber: PhoneNumber) -> Unit, allowCallAction: (() -> Unit)?,
                                listeners: MutableList<ListenerRegistration>) {
        userDocument.addSnapshotListenerLogException{ snapshot ->
            val userCountries = snapshot?.get(COUNTRIES_KEY) as MutableList<String>?
            if(userCountries.isNullOrEmpty()) {
                allowCallAction?.invoke()
            } else {
                val countriesCounter = if(allowCallAction != null) AtomicInteger(0) else null
                userCountries.forEach { country ->
                    findCountryBlockedNumberQuery(country, number).addSnapshotListenerLogException { snapshot ->
                        snapshot?.documents?.firstOrNull()?.run {
                            val parser = getPhoneNumberParser()
                            endCallAction.invoke(parser.parseSnapshot(this))
                            countriesCounter?.decrementAndGet()
                        }
                        if(countriesCounter?.incrementAndGet() == userCountries.size) {
                            allowCallAction!!.invoke()
                        }
                    }.run { listeners.add(this) }
                }
            }
        }.run { listeners.add(this) }
    }

    fun checkBlockEndCall(number: String, endCallAction: (phoneNumber: PhoneNumber) -> Unit, allowCallAction: (() -> Unit)? = null) : List<ListenerRegistration> =
        findAllowedNumberQuery(number).addSnapshotListenerLogException { snapshot ->
            if (snapshot?.isEmpty == true) {
                endCallAction.invoke(PhoneNumber(number))
            } else {
                allowCallAction?.invoke()
            }
        }.run { listOf(this) }

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