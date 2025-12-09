package com.example.dried_shrimp

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Fragment_Home: Fragment() {
    val sizeInDp = 24

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, null)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var etserach: EditText =view.findViewById(R.id.etserach)
        var img_cart : ImageView = view.findViewById(R.id.img_cart)
        var img_chat : ImageView = view.findViewById(R.id.img_chat)
        var recycle_home : RecyclerView = view.findViewById(R.id.recycle_home)

        val customAdapter = guesslike_Adapter()
        recycle_home.layoutManager = GridLayoutManager(context,2)
        recycle_home.adapter = customAdapter



        val sizeInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, sizeInDp.toFloat(), resources.displayMetrics
        ).toInt()
        val searchIcon = ContextCompat.getDrawable(context, R.drawable.search)
        val cameraIcon = ContextCompat.getDrawable(context, R.drawable.camera)

        searchIcon?.setBounds(0, 0, sizeInPx, sizeInPx)
        cameraIcon?.setBounds(0, 0, sizeInPx, sizeInPx)
        etserach.setCompoundDrawables(searchIcon, null, cameraIcon, null)


        img_cart.setOnClickListener {
            val intent = Intent(context, shopping_cart::class.java)
            startActivity(intent)
        }
    }



}