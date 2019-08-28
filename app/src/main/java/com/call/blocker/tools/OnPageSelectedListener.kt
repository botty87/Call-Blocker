package com.call.blocker.tools

import androidx.viewpager.widget.ViewPager

interface OnPageSelectedListener: ViewPager.OnPageChangeListener {
    override fun onPageScrollStateChanged(state: Int) {}

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
}