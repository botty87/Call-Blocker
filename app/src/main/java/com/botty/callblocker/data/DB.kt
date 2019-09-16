package com.botty.callblocker.data

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.provider.Settings
import androidx.work.ListenableWorker
import com.botty.callblocker.R
import com.botty.callblocker.data.country.CountriesLiveData
import com.botty.callblocker.fragments.allowedBlockedFragment.AllowedBlockedSuperFragment
import com.botty.callblocker.fragments.countriesFragment.CountriesFragment
import com.botty.callblocker.receiver.calls.CommonBlockTools
import com.botty.callblocker.settingsActivity.SettingsActivity
import com.botty.callblocker.tools.getUser
import com.botty.callblocker.tools.log
import com.firebase.ui.firestore.SnapshotParser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val USERS_KEY = "users"
private const val BLOCKED_KEY = "blocked"
private const val ALLOWED_KEY = "allowed"
private const val NUMBER_FIELD_KEY = "number"
private const val DESCRIPTION_FIELD_KEY = "description"
const val COUNTRIES_KEY = "countries"
private const val SYNC_REPORTS_KEY = "sync reports"

val userDocument: DocumentReference
    get() {
        return Firebase.firestore.collection(USERS_KEY).document(getUser()!!.uid)
    }

private val userBlockedRef: CollectionReference
    get() {
        return Firebase.firestore.collection(USERS_KEY).document(getUser()!!.uid).collection(
            BLOCKED_KEY
        )
    }

private val userAllowedRef: CollectionReference
    get() {
        return Firebase.firestore.collection(USERS_KEY).document(getUser()!!.uid).collection(
            ALLOWED_KEY
        )
    }

private val countriesRef: CollectionReference
    get() {
        return Firebase.firestore.collection(COUNTRIES_KEY)
    }

suspend fun AllowedBlockedSuperFragment.addAllowedPhone(phoneNumber: PhoneNumber) {
    addPhone(phoneNumber, userAllowedRef)
}

suspend fun AllowedBlockedSuperFragment.addBlockedPhone(phoneNumber: PhoneNumber) {
    addPhone(phoneNumber, userBlockedRef)
}

private suspend fun AllowedBlockedSuperFragment.addPhone(phoneNumber: PhoneNumber, phonesRef: CollectionReference) = suspendCoroutine<Void> { continuation ->

    fun addNumber() {
        //in case of restore we already have an id
        val document = if(phoneNumber.id.isNullOrEmpty()) phonesRef.document() else phonesRef.document(phoneNumber.id)

        document.set(phoneNumber)
            .addOnSuccessListener(this.activity!!) { continuation.resume(it) }
            .addOnFailureListener(this.activity!!) { exception ->
                exception.log()
                continuation.resumeWithException(exception)
            }
    }

    //Check if number is already saved. Than, if not, add the number
    phonesRef.whereEqualTo(NUMBER_FIELD_KEY, phoneNumber.number).get()
        .addOnSuccessListener(this.activity!!) { snapshot ->
            if(snapshot.isEmpty) {
                addNumber()
            }
            else {
                continuation.resumeWithException(Exception(getString(R.string.phone_number_already_saved)))
            }
        }
        .addOnFailureListener(this.activity!!) { exception ->
            exception.log()
            continuation.resumeWithException(exception)
        }
}

fun getUserBlockedNumbersQuery() = userBlockedRef
    .orderBy(DESCRIPTION_FIELD_KEY).orderBy(NUMBER_FIELD_KEY)

fun findBlockedNumberQuery(number: String) = userBlockedRef.whereEqualTo(NUMBER_FIELD_KEY, number)

fun findAllowedNumberQuery(number: String) = userAllowedRef.whereEqualTo(NUMBER_FIELD_KEY, number)

fun getUserAllowedNumbersQuery() = userAllowedRef
    .orderBy(DESCRIPTION_FIELD_KEY).orderBy(NUMBER_FIELD_KEY)

fun getPhoneNumberParser() = SnapshotParser { snapshot ->
    val phone = snapshot.getString(NUMBER_FIELD_KEY)!!
    val description = snapshot.getString(DESCRIPTION_FIELD_KEY)
    PhoneNumber(phone, description, snapshot.id)
}

fun AllowedBlockedSuperFragment.removeBlockedPhone(phoneNumber: PhoneNumber) {
    userBlockedRef.document(phoneNumber.id!!).delete()
}

fun AllowedBlockedSuperFragment.removeAllowedPhone(phoneNumber: PhoneNumber) {
    userAllowedRef.document(phoneNumber.id!!).delete()
}

fun Activity.cacheCountryData(country: String) {
    countriesRef.document(country).collection(BLOCKED_KEY).get(Source.SERVER)
        .addOnFailureListener(this) { it.log() }
}

fun CountriesFragment.updateUserCountriesDB(countriesLiveData: CountriesLiveData) {
    launch(Dispatchers.Default) {
        countriesLiveData.userUpdate = true
        countriesLiveData.value!!.filter { it.selected }.map { it.code }.let {countriesCode ->
            userDocument.set(hashMapOf(COUNTRIES_KEY to countriesCode))
                .addOnFailureListener(activity!!) { it.log() }
        }
    }
}

fun CommonBlockTools.findCountryBlockedNumberQuery(country: String, number: String) =
    countriesRef.document(country).collection(BLOCKED_KEY).whereEqualTo(NUMBER_FIELD_KEY, number)


suspend fun SettingsActivity.deleteUserData() = suspendCoroutine<Void> { continuation ->
    userDocument.delete()
        .addOnSuccessListener(this) { continuation.resume(it) }
        .addOnFailureListener(this) { exception ->
            exception.log()
            continuation.resumeWithException(exception)
        }
}

object DB{

    private const val TOTAL_OPERATIONS = 3
    suspend fun sync(context: Context) = suspendCoroutine<ListenableWorker.Result> { continuation ->
        val isNotResumed = AtomicBoolean(true)
        val startTime = Date()
        val operationsCompleted = AtomicInteger(0)

        @SuppressLint("HardwareIds")
        fun logSyncCompleted(exception: Exception? = null) {
            val deviceID = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            val endTime = Date()
            val report = SyncReport(deviceID, startTime, endTime, exception?.message)
            userDocument.collection(SYNC_REPORTS_KEY).document().set(report)
        }

        fun resume(result: ListenableWorker.Result) {
            if(isNotResumed.getAndSet(false)) {
                continuation.resume(result)
            }
        }

        fun markOperationCompletedAndContinue() {
            if(operationsCompleted.incrementAndGet() == TOTAL_OPERATIONS) {
                logSyncCompleted()
                resume(ListenableWorker.Result.success())
            }
        }

        fun onFail(e: Exception) {
            e.log()
            logSyncCompleted(e)
            resume(ListenableWorker.Result.failure())
        }

        fun retrieveCountriesList(snapshot: DocumentSnapshot) {
            val userCountries = snapshot.get(COUNTRIES_KEY) as MutableList<String>?
            userCountries?.run {
                val countriesCounter = AtomicInteger(0)
                fun markCountryCompletedAndContinue() {
                    if(countriesCounter.incrementAndGet() == this.size) {
                        markOperationCompletedAndContinue()
                    }
                }

                this.forEach { country ->
                    countriesRef.document(country).collection(BLOCKED_KEY).get(Source.SERVER)
                        .addOnSuccessListener { markCountryCompletedAndContinue() }
                        .addOnFailureListener (::onFail)
                }

            } ?: markOperationCompletedAndContinue()
        }

        userBlockedRef.get(Source.SERVER)
            .addOnSuccessListener { markOperationCompletedAndContinue() }
            .addOnFailureListener (::onFail)

        userAllowedRef.get(Source.SERVER)
            .addOnSuccessListener { markOperationCompletedAndContinue() }
            .addOnFailureListener (::onFail)

        userDocument.get(Source.SERVER)
            .addOnSuccessListener (::retrieveCountriesList)
            .addOnFailureListener (::onFail)
    }
}