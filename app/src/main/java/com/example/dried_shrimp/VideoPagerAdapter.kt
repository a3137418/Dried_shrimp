package com.example.dried_shrimp


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class VideoPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val videoPaths: List<String> // 傳入一個包含所有影片路徑的列表
) : FragmentStateAdapter(fragmentActivity) {

    /**
     * 回傳影片的總數
     */
    override fun getItemCount(): Int {
        return videoPaths.size
    }

    /**
     * 在需要顯示新的 Fragment 時被呼叫。
     * 我們根據位置(position)從影片路徑列表中取出對應的路徑，
     * 並建立一個新的 VideoPlayerFragment 實例。
     */
    override fun createFragment(position: Int): Fragment {
        val videoPath = videoPaths[position]
        return VideoPlayerFragment.newInstance(videoPath)
    }
}
