package com.vyw.tflite

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Contacts_permission : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts_permission)

        val nextBtn = findViewById<Button>(R.id.nextButton_5)
        nextBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        /*val backBtn = findViewById<Button>(R.id.backButton_5)
        backBtn.setOnClickListener {
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
            finish()
        }*/

        val skipButton = findViewById<Button>(R.id.skipButton)
        skipButton.setOnClickListener {
            // Proceed to the main menu
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


    }
}