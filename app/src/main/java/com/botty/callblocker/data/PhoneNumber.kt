package com.botty.callblocker.data

import android.view.View
import com.google.firebase.firestore.Exclude

data class PhoneNumber(
    val number: String,
    var description: String? = null,
    @get: Exclude
    val id: String? = null) {

    @get: Exclude
    val descriptionVisibility: Int get() {
        return if(description.isNullOrEmpty()) View.GONE else View.VISIBLE
    }
}