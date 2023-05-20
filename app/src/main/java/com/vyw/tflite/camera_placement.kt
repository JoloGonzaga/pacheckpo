package com.vyw.tflite

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class camera_placement : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_placement)

        val nextBtn = findViewById<Button>(R.id.nextButton)
        nextBtn.setOnClickListener {
            val intent = Intent(this, Location_permission::class.java)
            startActivity(intent)
            finish()
        }

        val backBtn = findViewById<Button>(R.id.backButton)
        backBtn.setOnClickListener {
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
            finish()
        }

        val skipButton = findViewById<Button>(R.id.skipButton)
        skipButton.setOnClickListener {
            // Proceed to the main menu
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}