package com.example.dried_shrimp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PurchasePagerAdapter(activity: FragmentActivity)
    : FragmentStateAdapter(activity) {

    override fun getItemCount() = 6

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> Fragment_Purchase_list_PendingPayment()
            1 -> Fragment_Purchase_list_PendingShipment()
            2 -> Fragment_Purchase_list_Receipt()
            3 -> Fragment_Purchase_list_OrderCompleted()
            4 -> Fragment_Purchase_list_Returnthegoods()
            5 -> Fragment_Purchase_list_notvalid()
            else -> Fragment_Purchase_list_PendingPayment()
        }
    }
}
