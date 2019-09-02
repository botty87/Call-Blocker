package com.call.blocker.fragments.allowedBlockedFragment


import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView

import com.call.blocker.R
import com.call.blocker.data.*

import com.call.blocker.tools.*
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.tomash.androidcontacts.contactgetter.main.contactsGetter.ContactsGetterBuilder
import es.dmoral.toasty.Toasty
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.android.synthetic.main.add_number_view.*
import kotlinx.android.synthetic.main.add_number_view.view.*
import kotlinx.android.synthetic.main.fragment_allowed_blocked.*
import kotlinx.coroutines.*

class AllowedBlockedSuperFragment(private val type: Type) : Fragment(), CoroutineScope by MainScope() {

    private var mainInterface: AllowedBlockedFragmentInterface? = null
    private val retrieveNumbersQuery by lazy {
        when(type) {
            Type.ALLOWED -> getAllowedNumbersQuery()
            Type.BLOCKED -> getBlockedNumbersQuery()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_allowed_blocked, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retrieveNumbers()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mainInterface = context as AllowedBlockedFragmentInterface
    }

    override fun onDetach() {
        super.onDetach()
        mainInterface = null
    }

    fun addNumberFabClicked() {
        fun addNumber(dialog: MaterialDialog, phone: String, description: String?) {
            fun lockDialog(locked: Boolean) = dialog.run {
                buttonAddNumber.isEnabled = !locked
                editTextDescription.isEnabled = !locked
                editTextPhoneNumber.isEnabled = !locked
            }

            val phoneNoSpaces = phone.replace("\\s+".toRegex(), "")

            dialog.getCustomView().run {
                launch {
                    hideKeyboard(editTextPhoneNumber)
                    progressBar.show()
                    lockDialog(true)
                    runCatching {
                        withContext(Dispatchers.IO) {
                            when(type) {
                                Type.ALLOWED -> addAllowedPhone(PhoneNumber(phoneNoSpaces, description))
                                Type.BLOCKED -> addBlockedPhone(PhoneNumber(phoneNoSpaces, description))
                            }
                        }
                    }.onSuccess {
                        dialog.dismiss()
                    }.onFailure { exception ->
                        logException(exception)
                        exception.localizedMessage?.run{ showErrorToast(this) }
                        lockDialog(false)
                        progressBar.hide()
                    }
                }
            }
        }

        fun setContactsSuggestion(dialog: MaterialDialog) {
            dialog.getCustomView().run {
                launch(Dispatchers.Default) {
                    val contacts = ContactsGetterBuilder(context)
                        .onlyWithPhones()
                        .buildList()
                        .map { ContactForAdapter(it) }

                    val adapter =
                        ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, contacts)
                    withContext(Dispatchers.Main) {
                        editTextDescription.setAdapter(adapter)
                        editTextDescription.onItemClickListener =
                            AdapterView.OnItemClickListener { adapterView, _, pos, _ ->
                                val contact = adapterView.getItemAtPosition(pos) as ContactForAdapter
                                editTextPhoneNumber.setText(contact.data.phoneList.first().mainData)
                            }
                    }
                }
            }
        }

        MaterialDialog(context!!, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            customView(R.layout.add_number_view, noVerticalPadding = true)
            getCustomView().run {
                buttonAddNumber.setOnClickListener {
                    var phone = editTextPhoneNumber.text?.toStringTrim()
                    if (phone?.isNotEmpty() == true) {
                        PhoneNumberUtil.createInstance(context).run {
                            val description =
                                editTextDescription.text?.toStringTrim()?.nullIfEmpty()
                            if (isPossibleNumber(phone, "")) {
                                addNumber(this@show, phone!!, description)
                            } else {
                                val region =
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        resources.configuration.locales[0].country
                                    } else {
                                        resources.configuration.locale.country
                                    }
                                val countryCode = getCountryCodeForRegion(region)
                                phone = "+$countryCode$phone"
                                if (isPossibleNumber(phone, region)) {
                                    addNumber(this@show, phone!!, description)
                                } else {
                                    showErrorToast(
                                        R.string.insert_valid_phone_number,
                                        Toasty.LENGTH_LONG
                                    )
                                }
                            }
                        }
                    } else {
                        showErrorToast(R.string.insert_valid_phone_number, Toasty.LENGTH_LONG)
                    }
                }
                if (SettingsContainer.readFromContacts) {
                    askPermission(Manifest.permission.READ_CONTACTS) {
                        setContactsSuggestion(this@show)
                    }.onDeclined {
                        SettingsContainer.readFromContacts = false
                        editTextDescription.setAdapter(null)
                    }
                }
            }
        }
    }

    private fun retrieveNumbers() {
        val options = FirestoreRecyclerOptions.Builder<PhoneNumber>()
            .setLifecycleOwner(this)
            .setQuery(retrieveNumbersQuery, getPhoneNumberParser())
            .build()

        recyclerViewNumbers.run {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = PhoneNumberAdapter(
                options,
                ::onPhoneNumberRemove
            )
        }
    }

    private fun onPhoneNumberRemove(phoneNumber: PhoneNumber) {
        when(type) {
            Type.ALLOWED -> removeAllowedPhone(phoneNumber)
            Type.BLOCKED -> removeBlockedPhone(phoneNumber)
        }
        mainInterface?.showUndoSnackbarBlockedPhoneRemoved {
            launch(Dispatchers.IO) {
                runCatching {
                    when(type) {
                        Type.ALLOWED -> addAllowedPhone(phoneNumber)
                        Type.BLOCKED -> addBlockedPhone(phoneNumber)
                    }
                }.onFailure { e ->
                    withContext(Dispatchers.Main) {
                        showErrorToast(R.string.error_re_adding_phone_number)
                    }
                    logException(e)
                }
            }
        }
    }

    enum class Type {ALLOWED, BLOCKED}
}
