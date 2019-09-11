package com.call.blocker.data

import android.app.Activity
import com.call.blocker.R
import com.call.blocker.fragments.allowedBlockedFragment.AllowedBlockedSuperFragment
import com.call.blocker.settingsActivity.SettingsActivity
import com.call.blocker.tools.getUser
import com.call.blocker.tools.log
import com.firebase.ui.firestore.SnapshotParser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val USERS_KEY = "users"
private const val BLOCKED_KEY = "blocked"
private const val ALLOWED_KEY = "allowed"
private const val NUMBER_FIELD_KEY = "number"
private const val DESCRIPTION_FIELD_KEY = "description"
private const val COUNTRIES_KEY = "countries"

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

suspend fun SettingsActivity.deleteUserData() = suspendCoroutine<Void> { continuation ->
    userDocument.delete()
        .addOnSuccessListener(this) { continuation.resume(it) }
        .addOnFailureListener(this) { exception ->
            exception.log()
            continuation.resumeWithException(exception)
        }
}