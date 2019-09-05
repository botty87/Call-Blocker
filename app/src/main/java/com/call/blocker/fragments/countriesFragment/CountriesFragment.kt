package com.call.blocker.fragments.countriesFragment

import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.ViewModelProvider
import com.call.blocker.R
import kotlinx.coroutines.*
import java.util.*

class CountriesFragment : Fragment(), CoroutineScope by MainScope() {

    private val viewModel by lazy { ViewModelProvider(this).get(CountriesFragmentViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_countries, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}
