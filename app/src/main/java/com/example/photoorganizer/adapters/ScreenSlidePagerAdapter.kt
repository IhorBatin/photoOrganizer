package com.example.photoorganizer.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.photoorganizer.view.ScreenSlidePageFragment

class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment = ScreenSlidePageFragment()
}