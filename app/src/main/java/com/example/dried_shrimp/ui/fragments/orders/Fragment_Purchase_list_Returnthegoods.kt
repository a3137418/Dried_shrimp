package com.example.dried_shrimp.ui.fragments.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dried_shrimp.databinding.FragmentPurchaselistReturnthegoodsBinding
import com.example.dried_shrimp.ui.adapters.GuessLikeAdapter

class Fragment_Purchase_list_Returnthegoods: Fragment() {
    private var binding: FragmentPurchaselistReturnthegoodsBinding ?=null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPurchaselistReturnthegoodsBinding.inflate(inflater,container,false)
        val view =binding?.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
    }

    private fun setupRecyclerViews() {
        val guesslike_Adapter = GuessLikeAdapter()
        binding?.sectionGuesslike?.myRecycleLike?.layoutManager = GridLayoutManager(requireContext(),2)
        binding?.sectionGuesslike?.myRecycleLike?.adapter = guesslike_Adapter

    }
}