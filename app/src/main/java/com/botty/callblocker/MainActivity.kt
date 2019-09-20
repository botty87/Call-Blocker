package com.botty.callblocker

import android.Manifest
import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.botty.callblocker.fragments.MyPagerAdapter
import com.botty.callblocker.fragments.allowedBlockedFragment.AllowedBlockedFragmentInterface
import com.botty.callblocker.fragments.allowedBlockedFragment.AllowedBlockedSuperFragment
import com.botty.callblocker.settingsActivity.SettingsActivity
import com.botty.callblocker.tools.*
import com.botty.callblocker.tools.snackBarProgressKotlin.SnackBarOnAction
import com.botty.callblocker.tools.snackBarProgressKotlin.SnackBarOnShown
import com.botty.callblocker.tools.sync.setSyncWorker
import com.crashlytics.android.Crashlytics
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.github.florent37.inlineactivityresult.kotlin.startForResult
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.ads.AdListener
import com.tingyik90.snackprogressbar.SnackProgressBar
import com.tingyik90.snackprogressbar.SnackProgressBarManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.startActivityForResult
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.google.android.gms.ads.AdRequest


class MainActivity : AppCompatActivity(), OnPageSelectedListener,
    AllowedBlockedFragmentInterface,
    CoroutineScope by MainScope() {

    private val snackProgressBarManager by lazy { SnackProgressBarManager(coordLayout, this) }
    private val pagerAdapter by lazy { MyPagerAdapter(supportFragmentManager, this) }

    @SuppressLint("WrongConstant", "NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fun endNoPermission() {
            showErrorToast(R.string.no_app_permission)
            finish()
        }

        askPermission(Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE, Manifest.permission.READ_CALL_LOG) {
            val version = android.os.Build.VERSION.SDK_INT
            when {
                version >= android.os.Build.VERSION_CODES.Q -> {
                    val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
                    val intent = roleManager.createRequestRoleIntent("android.app.role.CALL_SCREENING")
                    startForResult(intent) {
                        init()
                    }.onFailed {
                        showErrorToast(R.string.no_app_permission)
                        finish()
                    }
                }

                version == android.os.Build.VERSION_CODES.P -> {
                    askPermission(Manifest.permission.ANSWER_PHONE_CALLS) {
                        init()
                    }.onDeclined {
                        endNoPermission()
                    }
                }

                else -> init()
            }
        }.onDeclined {
            endNoPermission()
        }
    }

    private fun init() {
        if(hasUser()) {
            adView.loadAdLogExceptions()
            setupViewPager()
            setSyncWorker()
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

        val loginIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setIsSmartLockEnabled(!BuildConfig.DEBUG)
            .setAvailableProviders(providers)
            .build()

        startForResult(loginIntent) {
            setupViewPager()
            setSyncWorker()
        }.onFailed { result ->
            val response = IdpResponse.fromResultIntent(result.data)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            startForResultTest<SettingsActivity> { result ->
                if(result.data?.getBooleanExtra(Constants.LOGOUT_RESULT_KEY, false) == true) {
                    launch {
                        delay(300)
                        login()
                    }
                }
            }
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
