package com.botty.callblocker.receiver.calls

import android.content.Context
import com.botty.callblocker.R
import com.botty.callblocker.data.*
import com.botty.callblocker.tools.Constants
import com.botty.callblocker.tools.addSnapshotListenerLogException
import com.botty.callblocker.tools.log
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import es.dmoral.toasty.Toasty
import io.karn.notify.Notify
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

object CommonBlockTools {

    fun allowAllExceptBlocked(number: String, endCallAction: (phoneNumber: PhoneNumber) -> Unit, allowCallAction: (() -> Unit)? = null)
            : List<ListenerRegistration> {

        val listeners = mutableListOf<ListenerRegistration>()
        val isAlreadyCheckingAllowedNumber = AtomicBoolean(false)

        fun checkAllowedNumber() {
            val isAlreadySearchingUserList = AtomicBoolean(false)
            //Sometimes could happen that a number is in a country list, but the user does not want to block it. So check the allowed list
            if(!isAlreadyCheckingAllowedNumber.getAndSet(true)) {
                findAllowedNumberQuery(number).addSnapshotListenerLogException { snapshot ->
                    if (snapshot?.isEmpty != false) {
                        if(!isAlreadySearchingUserList.getAndSet(true)) {
                            searchUserLists(number, endCallAction, allowCallAction, listeners)
                        }
                    } else {
                        allowCallAction?.invoke()
                    }
                }.run { listeners.add(this) }
            }
        }

        findBlockedNumberQuery(number).addSnapshotListenerLogException { snapshot ->
            snapshot?.documents?.firstOrNull()?.run {
                val phoneNumber = getPhoneNumberParser().parseSnapshot(this)
                checkMultipleCalls(phoneNumber, endCallAction, allowCallAction)
            } ?: checkAllowedNumber()
        }.run { listeners.add(this) }
        return listeners
    }

    fun blockAllExceptAllowed(number: String, endCallAction: (phoneNumber: PhoneNumber) -> Unit, allowCallAction: (() -> Unit)? = null) : List<ListenerRegistration> =
        findAllowedNumberQuery(number).addSnapshotListenerLogException { snapshot ->
            if (snapshot?.isEmpty == true) {
                checkMultipleCalls(PhoneNumber(number), endCallAction, allowCallAction)
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

    private fun searchUserLists(number: String, endCallAction: (phoneNumber: PhoneNumber) -> Unit, allowCallAction: (() -> Unit)?,
                                listeners: MutableList<ListenerRegistration>) {
        userDocument.addSnapshotListenerLogException { snapshot ->
            val userCountries = snapshot?.get(COUNTRIES_KEY) as MutableList<String>?
            if(userCountries.isNullOrEmpty()) {
                allowCallAction?.invoke()
            } else {
                val countriesCounter = if(allowCallAction != null) AtomicInteger(0) else null
                userCountries.forEach { country ->
                    findCountryBlockedNumberQuery(country, number).addSnapshotListenerLogException { snapshot ->
                        snapshot?.documents?.firstOrNull()?.run {
                            val phoneNumber = getPhoneNumberParser().parseSnapshot(this)
                            checkMultipleCalls(phoneNumber, endCallAction, allowCallAction)
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

    private fun checkMultipleCalls(phoneNumber: PhoneNumber, endCallAction: (phoneNumber: PhoneNumber) -> Unit, allowCallAction: (() -> Unit)?) {
        fun endCallAndLog(endCallAction: (phoneNumber: PhoneNumber) -> Unit, phoneNumber: PhoneNumber) {
            endCallAction.invoke(phoneNumber)
            BlockedCall(phoneNumber).log()
        }

        phoneNumber.run {
            SettingsContainer.run {
                if(ringOnMultipleCalls && (calls <= 0 || minutes <= 0)) {
                    ringOnMultipleCalls = false
                }
                if(ringOnMultipleCalls) {
                    getMultipleCallsQuery(number, calls, minutes).get(Source.CACHE)
                        .addOnSuccessListener { snapshot ->
                            if (snapshot.size() >= calls) {
                                allowCallAction?.invoke()
                            } else {
                                endCallAndLog(endCallAction, phoneNumber)
                            }
                        }.addOnFailureListener { e ->
                            e.log()
                            endCallAndLog(endCallAction, phoneNumber)
                        }
                } else {
                    endCallAndLog(endCallAction, phoneNumber)
                }
            }
        }
    }
}