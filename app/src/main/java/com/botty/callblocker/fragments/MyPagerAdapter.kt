package com.botty.callblocker.fragments

import android.content.Context
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.botty.callblocker.R
import com.botty.callblocker.fragments.countriesFragment.CountriesFragment
import com.botty.callblocker.fragments.allowedBlockedFragment.AllowedBlockedSuperFragment
import com.botty.callblocker.fragments.allowedBlockedFragment.AllowedBlockedSuperFragment.Type

private const val TOTAL_FRAGMENTS = 3

class MyPagerAdapter(fm: FragmentManager, private val context: Context): FragmentPagerAdapter(fm) {

    lateinit var activeFragment: Fragment
    private set
    
    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> AllowedBlockedSuperFragment(Type.BLOCKED)
            1 -> AllowedBlockedSuperFragment(Type.ALLOWED)
            2 -> CountriesFragment()
            else -> throw Exception("Wrong index!")
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position) {
            0 -> context.getString(R.string.blocked)
            1 -> context.getString(R.string.allowed)
            2 -> context.getString(R.string.countries)
            else -> throw Exception("Wrong index!")
        }
    }

    override fun getCount(): Int {
        return TOTAL_FRAGMENTS
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
        activeFragment = `object` as Fragment
    }
}