package com.example.dried_shrimp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dried_shrimp.databinding.ActivityMoreServeBinding

class Activity_more_serve : AppCompatActivity() {
    private lateinit var binding: ActivityMoreServeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_more_serve)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityMoreServeBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        setupRecyclerViews()
        back()
    }

    private fun setupRecyclerViews() {
        val more_serive_Adapter = more_serive_Adapter()
        binding?.moreseriveRecycle?.layoutManager = GridLayoutManager(this, 2)
        binding?.moreseriveRecycle?.adapter = more_serive_Adapter
    }
    fun back(){
        val back = binding.moerseriveImgBack
        back.setOnClickListener {
            finish()
        }
    }
}