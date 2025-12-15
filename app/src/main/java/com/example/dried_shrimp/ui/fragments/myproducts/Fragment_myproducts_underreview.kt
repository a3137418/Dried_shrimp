package com.example.dried_shrimp.ui.fragments.myproducts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dried_shrimp.data.model.Product
import com.example.dried_shrimp.databinding.FragmentMyproductsUnderreviewBinding
import com.example.dried_shrimp.ui.adapters.GuessLikeAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Fragment_myproducts_underreview : Fragment() {

    private var _binding: FragmentMyproductsUnderreviewBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: GuessLikeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyproductsUnderreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 假設 layout 裡面的 RecyclerView ID 是 recyclerView
        adapter = GuessLikeAdapter(emptyList())
        binding.recycleUnderreview.layoutManager = LinearLayoutManager(context)
        binding.recycleUnderreview.adapter = adapter

        val userId = auth.currentUser?.uid ?: return

        // 查詢：審核中
        db.collection("products")
            .whereEqualTo("sellerId", userId)
            .whereEqualTo("status", "UNDER_REVIEW")
            .get()
            .addOnSuccessListener { result ->
                adapter.updateData(result.toObjects(Product::class.java))
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}