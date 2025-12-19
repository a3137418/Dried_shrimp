package com.example.dried_shrimp.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.dried_shrimp.R
import com.example.dried_shrimp.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class Fragment_EditProfile : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var selectedImageUri: Uri? = null

    // 註冊選圖功能
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            binding.ivProfileAvatar.setImageURI(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupInitialData()
        setupListeners()
    }

    private fun setupInitialData() {
        val user = auth.currentUser
        if (user != null) {
            binding.etNickname.setText(user.displayName)

            if (user.photoUrl != null) {
                Glide.with(this)
                    .load(user.photoUrl)
                    .circleCrop()
                    .into(binding.ivProfileAvatar)
            }
        }
    }

    private fun setupListeners() {
        // 返回按鈕：直接關閉當前的 Activity
        binding.btnBack.setOnClickListener {
            // 如果你是用新的 Activity 裝這個 Fragment，用 finish() 才會關閉頁面
            requireActivity().finish()

            // 備註：如果你是單純 Fragment 跳轉且有 addToBackStack，才用 parentFragmentManager.popBackStack()
            // 但依照我們之前的寫法 (開新視窗)，用 finish() 才是對的。
        }

        // 點擊頭像選圖
        binding.ivProfileAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // 儲存
        binding.btnSave.setOnClickListener {
            updateProfile()
        }
    }

    private fun updateProfile() {
        val user = auth.currentUser ?: return
        val newName = binding.etNickname.text.toString().trim()

        if (newName.isEmpty()) {
            binding.tilNickname.error = "暱稱不能為空"
            return
        }

        setLoading(true)

        // 情境 A: 有換新圖片 -> 上傳後更新
        if (selectedImageUri != null) {
            uploadImageToStorage(selectedImageUri!!) { imageUrl ->
                if (imageUrl != null) {
                    saveToAuthAndFirestore(newName, imageUrl)
                } else {
                    setLoading(false)
                    Toast.makeText(context, "圖片上傳失敗", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // 情境 B: 沒換圖片 -> 只更新文字
            saveToAuthAndFirestore(newName, user.photoUrl)
        }
    }

    private fun uploadImageToStorage(uri: Uri, onComplete: (Uri?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val ref = storage.reference.child("users/$uid/profile.jpg")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    onComplete(downloadUri)
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    private fun saveToAuthAndFirestore(name: String, photoUri: Uri?) {
        val user = auth.currentUser ?: return

        // 1. 更新 Firebase Auth (這部分保持不變)
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .setPhotoUri(photoUri)
            .build()

        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // 2. 更新 Firestore
                val userData = hashMapOf<String, Any>("name" to name)
                if (photoUri != null) {
                    userData["photoUrl"] = photoUri.toString()
                    userData["imageUrl"] = photoUri.toString()
                }

                // ★★★ 關鍵修改：將 .update(...) 改成 .set(..., SetOptions.merge()) ★★★
                db.collection("users").document(user.uid)
                    .set(userData, com.google.firebase.firestore.SetOptions.merge()) // 這樣寫最保險
                    .addOnSuccessListener {
                        setLoading(false)
                        Toast.makeText(context, "更新成功", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
                    .addOnFailureListener { e ->
                        setLoading(false)
                        // 把錯誤訊息印出來，方便我們除錯
                        android.util.Log.e("EditProfile", "Firestore Error", e)
                        Toast.makeText(context, "資料庫儲存失敗: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                setLoading(false)
                Toast.makeText(context, "Auth 更新失敗: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (binding == null) return
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSave.isEnabled = false
            binding.btnSave.text = "更新中..."
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnSave.isEnabled = true
            binding.btnSave.text = "儲存變更"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}