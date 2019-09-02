package com.call.blocker

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import com.afollestad.materialdialogs.MaterialDialog
import com.call.blocker.fragments.MyPagerAdapter
import com.call.blocker.fragments.allowedBlockedFragment.AllowedBlockedSuperFragment
import com.call.blocker.fragments.allowedBlockedFragment.AllowedBlockedFragmentInterface
import com.call.blocker.settingsActivity.SettingsActivity
import com.call.blocker.tools.*
import com.call.blocker.tools.Constants.LOGIN_REQ_CODE
import com.call.blocker.tools.Constants.SETTINGS_ACTIVITY_REQ_CODE
import com.call.blocker.tools.snackBarProgressKotlin.SnackBarOnAction
import com.call.blocker.tools.snackBarProgressKotlin.SnackBarOnShown
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.tingyik90.snackprogressbar.SnackProgressBar
import com.tingyik90.snackprogressbar.SnackProgressBarManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import org.jetbrains.anko.startActivityForResult

import android.content.Context
import android.telecom.TelecomManager
import androidx.core.content.getSystemService
import androidx.core.role.RoleManagerCompat
import com.call.blocker.tools.Constants.CALL_SCREENING_REQ_CODE


class MainActivity : AppCompatActivity(), OnPageSelectedListener,
    AllowedBlockedFragmentInterface,
    CoroutineScope by MainScope() {

    private val snackProgressBarManager by lazy { SnackProgressBarManager(coordLayout, this) }
    private val pagerAdapter by lazy { MyPagerAdapter(supportFragmentManager, this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askPermission(Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE, Manifest.permission.READ_CALL_LOG) {

            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
                val intent = roleManager.createRequestRoleIntent("android.app.role.CALL_SCREENING")
                startActivityForResult(intent, Constants.CALL_SCREENING_REQ_CODE)
            }
            else {
                init()
            }
        }.onDeclined {
            showErrorToast(R.string.no_app_permission)
            finish()
        }
    }

    private fun init() {
        if(hasUser()) {
            setupViewPager()
        }
        else {
            login()
        }
    }

    private fun setupViewPager() {
        viewPager.adapter = pagerAdapter
        viewPager.addOnPageChangeListener(this)
        navigationBar.setOnNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                R.id.blocked -> viewPager.currentItem = 0
                R.id.allowed -> viewPager.currentItem = 1
                R.id.countries -> viewPager.currentItem = 2
                else -> throw Exception("Wrong index")
            }
            true
        }
        fabAddNumber.setOnClickListener {
            (pagerAdapter.activeFragment as AllowedBlockedSuperFragment).addNumberFabClicked()
        }
    }

    private fun login() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())

        startActivityForResult(AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setIsSmartLockEnabled(!BuildConfig.DEBUG)
            .setAvailableProviders(providers)
            .build(),
            LOGIN_REQ_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            LOGIN_REQ_CODE -> {
                val response = IdpResponse.fromResultIntent(data)

                if (resultCode == RESULT_OK) {
                    setupViewPager()
                } else {
                    response?.error?.run {
                        MaterialDialog(this@MainActivity).show {
                            val errorMessage = "$localizedMessage ${getString(R.string.retry)}"
                            title(R.string.error)
                            message(text = errorMessage)
                            cancelable(false)
                            negativeButton(R.string.no) { finish() }
                            positiveButton(R.string.yes) { login() }
                        }
                    } ?: finish()
                }
            }

            SETTINGS_ACTIVITY_REQ_CODE -> {
                if(resultCode == RESULT_OK && data?.getBooleanExtra(Constants.LOGOUT_RESULT_KEY, false) == true) {
                    login()
                }
            }

            CALL_SCREENING_REQ_CODE -> {
                if(resultCode == android.app.Activity.RESULT_OK) {
                    init()
                }
                else {
                    showErrorToast(R.string.no_app_permission)
                    finish()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            startActivityForResult<SettingsActivity>(SETTINGS_ACTIVITY_REQ_CODE)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPageSelected(position: Int) {
        when(position) {
            0 -> {
                navigationBar.selectedItemId = R.id.blocked
                fabAddNumber.show()
                fabAddNumber.setOnClickListener {
                    (pagerAdapter.activeFragment as AllowedBlockedSuperFragment).addNumberFabClicked()
                }
            }
            1 -> {
                navigationBar.selectedItemId = R.id.allowed
                fabAddNumber.show()
                fabAddNumber.setOnClickListener {
                    (pagerAdapter.activeFragment as AllowedBlockedSuperFragment).addNumberFabClicked()
                }
            }
            2 -> {
                navigationBar.selectedItemId = R.id.countries
                fabAddNumber.hide()
                fabAddNumber.setOnClickListener(null)
            }
            else -> throw Exception("Wrong index")
        }
    }

    //Fragments callbacks
    override fun showUndoSnackbarBlockedPhoneRemoved(undoAction: () -> Unit) {
        val countDownTime = 5000
        val countDownTimer = object: CountDownTimer(countDownTime.toLong(), 60) {
            override fun onTick(time: Long) {
                snackProgressBarManager.setProgress(countDownTime - time.toInt())
            }

            override fun onFinish() {
                snackProgressBarManager.dismissAll()
                snackProgressBarManager.setOnDisplayListener(null)
            }
        }

        val snackProgressBar = SnackProgressBar(SnackProgressBar.TYPE_HORIZONTAL, getString(R.string.number_removed))
            .setSwipeToDismiss(true)
            .setProgressMax(countDownTime)
            .setAllowUserInput(true)
            .setAction(getString(R.string.undo),
                SnackBarOnAction {
                    snackProgressBarManager.dismissAll()
                    snackProgressBarManager.setOnDisplayListener(null)
                    countDownTimer.cancel()
                    undoAction.invoke()
                })

        snackProgressBarManager.setOnDisplayListener(SnackBarOnShown {
            countDownTimer.start()
        })

        snackProgressBarManager.setProgress(0)
        snackProgressBarManager.show(snackProgressBar, SnackProgressBarManager.LENGTH_INDEFINITE)
    }
}
