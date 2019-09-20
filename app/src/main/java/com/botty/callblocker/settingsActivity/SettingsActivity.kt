package com.botty.callblocker.settingsActivity

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.botty.callblocker.R
import com.botty.callblocker.data.SettingsContainer
import com.botty.callblocker.data.deleteUserData
import com.botty.callblocker.databinding.ActivitySettingsBinding
import com.botty.callblocker.tools.*
import com.firebase.ui.auth.AuthUI
import com.github.florent37.kotlin.pleaseanimate.please
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.tingyik90.snackprogressbar.SnackProgressBar
import com.tingyik90.snackprogressbar.SnackProgressBarManager
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.coroutines.*

class SettingsActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val snackProgressBarManager by lazy { SnackProgressBarManager(mainLayout, this) }

    private val settings by lazy { ViewModelProvider(this).get(SettingsViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        fun checkContactsPermission() {
            askPermission(Manifest.permission.READ_CONTACTS) {}.onDeclined {
                settings.readFromContacts.value = false
            }
        }

        fun setCheckers() {
            settings.readFromContacts.observe(this) { readFromContacts ->
                if(readFromContacts) {
                    checkContactsPermission()
                }
            }
        }

        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivitySettingsBinding>(this, R.layout.activity_settings).let { binding ->
            binding.lifecycleOwner = this
            binding.settings = this@SettingsActivity.settings

            //Check if we have all the permissions
            if(settings.readFromContacts.value!!) {
                checkContactsPermission()
            }
        }

        setCheckers()

        getUser()?.run {
            textViewName.text = displayName ?: getString(R.string.no_user_name)
            textViewEmailPhone.text = email ?: phoneNumber
        }

        buttonLogout.setOnClickListener { logoutUser() }
        buttonDelete.setOnClickListener { deleteUser() }

        settings.ringOnMultipleCalls.observe(this) { enabled ->
            please(200) {
                animate(layoutRepeatedCallsDetails) {
                    if(enabled) {
                        visible()
                        originalScale()
                    } else {
                        invisible()
                        scale(1f, 0f)
                    }
                }
            }.start()
        }
    }

    private fun deleteUser() {
        fun performDelete() {
            settings.buttonsEnabled.value = false
            val snackProgress = SnackProgressBar(SnackProgressBar.TYPE_CIRCULAR, getString(R.string.user_delete))
                .setSwipeToDismiss(false)
                .setIsIndeterminate(true)

            SnackProgressBarManager(mainLayout, this@SettingsActivity)
                .show(snackProgress, SnackProgressBarManager.LENGTH_INDEFINITE)

            launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        deleteUserData()
                    }
                }.onSuccess {
                    getUser()!!.delete().addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            SettingsContainer.resetToDefault()
                            Intent().apply { putExtra(Constants.LOGOUT_RESULT_KEY, true) }.run {
                                setResult(Activity.RESULT_OK, this)
                                finish()
                            }
                        } else {
                            task.exception?.log()
                            showErrorToast(R.string.error_user_delete)
                            settings.buttonsEnabled.value = true
                        }
                    }
                }.onFailure {
                    showErrorToast(R.string.error_user_delete)
                    settings.buttonsEnabled.value = true
                }
            }
        }

        MaterialDialog(this).show {
            title(R.string.delete)
            message(text = getString(R.string.confirm_delete))
            negativeButton(R.string.no)
            positiveButton(R.string.yes) { performDelete() }
        }
    }

    private fun logoutUser() {
        fun performLogout() {
            settings.buttonsEnabled.value = false
            val snackProgress = SnackProgressBar(SnackProgressBar.TYPE_CIRCULAR, getString(R.string.user_logout))
                .setSwipeToDismiss(false)
                .setIsIndeterminate(true)

            snackProgressBarManager.show(snackProgress, SnackProgressBarManager.LENGTH_INDEFINITE)

            AuthUI.getInstance().signOut(this)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        SettingsContainer.resetToDefault()
                        Intent().apply { putExtra(Constants.LOGOUT_RESULT_KEY, true) }.run {
                            setResult(Activity.RESULT_OK, this)
                            finish()
                        }
                    }
                    else {
                        task.exception?.log()
                        showErrorToast(R.string.error_user_logout)
                        settings.buttonsEnabled.value = true
                    }
                }
        }

        MaterialDialog(this).show {
            title(R.string.logout)
            message(text = getString(R.string.confirm_logout))
            negativeButton(R.string.no)
            positiveButton(R.string.yes) { performLogout() }
        }
    }
}
