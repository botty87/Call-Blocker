package com.call.blocker.tools

import android.content.Context
import android.text.Editable
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import timber.log.Timber
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import es.dmoral.toasty.Toasty
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun hasUser(): Boolean {
    return FirebaseAuth.getInstance().currentUser != null
}

fun getUser(): FirebaseUser? {
    return FirebaseAuth.getInstance().currentUser
}

fun Context.showErrorToast(message: String) {
    Toasty.error(this, message).show()
}

fun Context.showErrorToast(stringID: Int, length: Int = Toasty.LENGTH_SHORT) {
    Toasty.error(this, stringID, length).show()
}

fun Fragment.showErrorToast(stringID: Int, length: Int = Toasty.LENGTH_SHORT) {
    Toasty.error(context!!, stringID, length).show()
}

fun Fragment.showErrorToast(message: String, length: Int = Toasty.LENGTH_SHORT) {
    Toasty.error(context!!, message, length).show()
}

fun Context.showWarningToast(stringID: Int, length: Int = Toasty.LENGTH_SHORT) {
    Toasty.warning(this, stringID, length).show()
}

fun logException(t: Throwable) {
    Timber.e(t)
    //TODO implements
}

fun Editable.toStringTrim(): String {
    return toString().trim()
}

fun String.nullIfEmpty(): String? {
    return if(isEmpty()) null else this
}

fun Context.hideKeyboard(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Fragment.hideKeyboard(view: View) {
    context!!.hideKeyboard(view)
}

fun <T> LiveData<T>.observe(owner: LifecycleOwner, action: ((T) -> Unit)) {
    this.observe(owner, Observer { value ->
        action.invoke(value)
    })
}