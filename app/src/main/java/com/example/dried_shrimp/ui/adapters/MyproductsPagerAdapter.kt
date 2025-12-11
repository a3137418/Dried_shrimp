package com.example.dried_shrimp.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.dried_shrimp.ui.fragments.myproducts.Fragment_myproducts_notavailable
import com.example.dried_shrimp.ui.fragments.myproducts.Fragment_myproducts_ontheshelf
import com.example.dried_shrimp.ui.fragments.myproducts.Fragment_myproducts_soldout
import com.example.dried_shrimp.ui.fragments.myproducts.Fragment_myproducts_underreview
import com.example.dried_shrimp.ui.fragments.myproducts.Fragment_myproducts_violation


class MyproductsPagerAdapter(activity: FragmentActivity)
    : FragmentStateAdapter(activity) {

    override fun getItemCount() = 5

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> Fragment_myproducts_ontheshelf()
            1 -> Fragment_myproducts_soldout()
            2 -> Fragment_myproducts_underreview()
            3 -> Fragment_myproducts_violation()
            4 -> Fragment_myproducts_notavailable()
            else -> Fragment_myproducts_ontheshelf()
        }
    }
}