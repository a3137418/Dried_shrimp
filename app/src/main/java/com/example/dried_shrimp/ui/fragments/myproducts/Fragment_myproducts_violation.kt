package com.example.dried_shrimp.ui.fragments.myproducts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dried_shrimp.data.model.Product
import com.example.dried_shrimp.databinding.FragmentMyproductsViolationBinding
import com.example.dried_shrimp.ui.adapters.GuessLikeAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Fragment_myproducts_violation : Fragment() {

    private var _binding: FragmentMyproductsViolationBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: GuessLikeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyproductsViolationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = GuessLikeAdapter(emptyList())
        binding.recycleViolation.layoutManager = LinearLayoutManager(context)
        binding.recycleViolation.adapter = adapter

        val userId = auth.currentUser?.uid ?: return

        // 查詢：違規
        db.collection("products")
            .whereEqualTo("sellerId", userId)
            .whereEqualTo("status", "VIOLATION")
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