package com.botty.callblocker.fragments.countriesFragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.botty.callblocker.R
import com.botty.callblocker.data.cacheCountryData
import com.botty.callblocker.data.country.Country
import com.botty.callblocker.data.updateUserCountriesDB
import com.botty.callblocker.tools.observe
import com.botty.callblocker.tools.onTextChanged
import kotlinx.android.synthetic.main.fragment_countries.*
import kotlinx.coroutines.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.Unregistrar

class CountriesFragment : Fragment(), CoroutineScope by MainScope(), CountryListener {

    private val viewModel by lazy { ViewModelProvider(this).get(CountriesFragmentViewModel::class.java) }
    //private val countriesLayoutManager by lazy { LinearLayoutManager(context, RecyclerView.VERTICAL, false) }
    private lateinit var countriesAdapter: CountriesAdapter
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(OnKeyboardLifecycleObserver(activity!!))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_countries, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fun initRecyclerViewCountries() {
            recyclerViewCountries.setHasFixedSize(true)
            recyclerViewCountries.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            recyclerViewCountries.adapter = CountriesAdapter().apply {
                countryListener = this@CountriesFragment
                countriesAdapter = this
            }
        }

        super.onViewCreated(view, savedInstanceState)
        initRecyclerViewCountries()

        viewModel.countries.observe(this) { countries ->
            filterCountry(editTextSearch.text?.toString(), countries)
        }

        editTextSearch.onTextChanged { text ->
            filterCountry(text, viewModel.countries.value ?: emptyList())
        }
    }

    private fun filterCountry(text: String?, allCountries: List<Country>) {
        searchJob?.cancel()
        if(text.isNullOrEmpty()) {
            countriesAdapter.setNewCountries(allCountries, true)
        }
        else {
            text.trim().let { search ->
                searchJob = launch {
                    val filteredCountries = withContext(Dispatchers.Default) {
                        allCountries.filter { country ->
                            country.name.startsWith(search, true)
                        }
                    }
                    withContext(NonCancellable) {
                        countriesAdapter.setNewCountries(filteredCountries)
                    }
                }
            }
        }
    }

    //Callback from the recyclerview adapter
    override fun onCountrySelected(country: Country) {
        if(country.selected) {
            activity?.cacheCountryData(country.code)
        }
        updateUserCountriesDB(viewModel.countries)
    }

    internal inner class OnKeyboardLifecycleObserver(private val activity: Activity) : LifecycleObserver {
        private var keyboardListener: Unregistrar? = null

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun setKeyboardListener() {
            keyboardListener = KeyboardVisibilityEvent.registerEventListener(activity) { visible ->
                if(!visible && editTextSearch.hasFocus()) {
                    editTextSearch.clearFocus()
                }
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun unsetKeyboardListener() {
            keyboardListener?.unregister()
            keyboardListener = null
        }
    }

}
