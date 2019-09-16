package com.botty.callblocker.tools

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import es.dmoral.toasty.Toasty
import timber.log.Timber

fun hasUser(): Boolean {
    return FirebaseAuth.getInstance().currentUser != null
}

fun getUser(): FirebaseUser? {
    return FirebaseAuth.getInstance().currentUser
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

fun Throwable.log() {
    Timber.e(this)
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

fun EditText.onTextChanged(listener: (String?) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(text: Editable?) {
            listener.invoke(text?.toString())
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    })
}

fun Query.addSnapshotListenerLogException(action: ((QuerySnapshot?) -> Unit)): ListenerRegistration =
    addSnapshotListener { snapshot, exception ->
        exception?.log()
        action.invoke(snapshot)
    }

fun DocumentReference.addSnapshotListenerLogException(action: ((DocumentSnapshot?) -> Unit)): ListenerRegistration =
    addSnapshotListener { snapshot, exception ->
        exception?.log()
        action.invoke(snapshot)
    }