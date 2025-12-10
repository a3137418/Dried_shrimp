package com.example.dried_shrimp.ui.activities

import android.os.Bundle
import android.util.TypedValue
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dried_shrimp.R
import com.example.dried_shrimp.ui.adapters.GuessLikeAdapter

class HomeActivity : AppCompatActivity() {
    lateinit var etserach: EditText
    lateinit var img_serach : ImageView
    lateinit var img_chat : ImageView
    lateinit var recycle_home : RecyclerView

    val sizeInDp = 24



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findview()
        adapter()

        supportActionBar?.setDisplayShowTitleEnabled(false)
        val sizeInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, sizeInDp.toFloat(), resources.displayMetrics
        ).toInt()
        val searchIcon = ContextCompat.getDrawable(this, R.drawable.search)
        val cameraIcon = ContextCompat.getDrawable(this, R.drawable.camera)

        searchIcon?.setBounds(0, 0, sizeInPx, sizeInPx)
        cameraIcon?.setBounds(0, 0, sizeInPx, sizeInPx)
        etserach.setCompoundDrawables(searchIcon, null, cameraIcon, null)



    }


    fun findview(){
        etserach =findViewById(R.id.etserach)
        img_serach = findViewById(R.id.img_cart)
        img_chat = findViewById(R.id.img_chat)
        recycle_home =findViewById(R.id.recycle_home)

    }

    fun adapter() {
        val customAdapter = GuessLikeAdapter()
        recycle_home.layoutManager = GridLayoutManager(this, 2)
        recycle_home.adapter = customAdapter

    }


}