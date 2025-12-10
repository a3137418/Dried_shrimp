package com.example.dried_shrimp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.dried_shrimp.R
import com.example.dried_shrimp.ui.adapters.VideoPagerAdapter

class Fragment_Live : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 載入包含 ViewPager2 的佈局
        return inflater.inflate(R.layout.fragment_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager: ViewPager2 = view.findViewById(R.id.viewPagerVideos)

        // 準備影片資料
        val videoList = listOf(
            "android.resource://${requireActivity().packageName}/${R.raw.video1}",
            "android.resource://${requireActivity().packageName}/${R.raw.video2}",
            "android.resource://${requireActivity().packageName}/${R.raw.video3}"
        )

        // 建立 Adapter，注意這裡傳入的是 `requireActivity()`
        val adapter = VideoPagerAdapter(requireActivity(), videoList)
        viewPager.adapter = adapter
    }
}