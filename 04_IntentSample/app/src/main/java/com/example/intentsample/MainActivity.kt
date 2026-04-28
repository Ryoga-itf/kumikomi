package com.example.intentsample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<android.widget.Button>(R.id.button_orientation).setOnClickListener {
            startActivity(Intent(this, OrientationActivity::class.java))
        }

        findViewById<android.widget.Button>(R.id.button_location).setOnClickListener {
            startActivity(Intent(this, LocationActivity::class.java))
        }
    }
}