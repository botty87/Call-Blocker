package com.call.blocker.fragments

import android.content.Context
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.call.blocker.R
import com.call.blocker.fragments.countriesFragment.CountriesFragment
import com.call.blocker.fragments.allowedBlockedFragment.AllowedBlockedSuperFragment
import com.call.blocker.fragments.allowedBlockedFragment.AllowedBlockedSuperFragment.Type

private const val TOTAL_FRAGMENTS = 3

class MyPagerAdapter(fm: FragmentManager, private val context: Context): FragmentPagerAdapter(fm) {

    lateinit var activeFragment: Fragment
    private set
    
    override fun getItem(position: Int): Fragment {
        return when(position) {
            2 -> AllowedBlockedSuperFragment(Type.BLOCKED) //TODO restore
            1 -> AllowedBlockedSuperFragment(Type.ALLOWED)
            0 -> CountriesFragment()
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