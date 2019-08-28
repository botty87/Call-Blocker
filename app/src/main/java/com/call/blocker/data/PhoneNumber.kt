package com.call.blocker.data

import android.view.View
import androidx.databinding.Bindable
import com.google.firebase.firestore.Exclude

data class PhoneNumber(
    val number: String,
    var description: String?,
    @get: Exclude
    val id: String? = null) {

    @get: Exclude
    val descriptionVisibility: Int get() {
        return if(description.isNullOrEmpty()) View.GONE else View.VISIBLE
    }
}