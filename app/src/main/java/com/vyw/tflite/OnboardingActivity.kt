package com.vyw.tflite

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // Check if it's the first time user
        /*sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val isFirstTimeUser = sharedPreferences.getBoolean("isFirstTimeUser", true)

        if (isFirstTimeUser) {
            // If it's the first time, show onboarding screen
            // Set isFirstTimeUser to false to avoid showing onboarding again
            sharedPreferences.edit().putBoolean("isFirstTimeUser", false).apply()
        } else {
            // If not the first time, go directly to the main menu
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return  // Skip the rest of the code
        }*/


        /*backBtn = findViewById<Button>(R.id.backButton)
        nextBtn = findViewById<Button>(R.id.nextButton)

        backBtn.setOnClickListener(View.OnClickListener {
            if (getitem(0) > 0) {
                mSLideViewPager.setCurrentItem(getitem(-1), true)
            }
        })*/

        val skipButton = findViewById<Button>(R.id.skipButton)
        skipButton.setOnClickListener {
            // Proceed to the main menu
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val nextButton = findViewById<Button>(R.id.nextButton_1)
        nextButton.setOnClickListener {
            val intent = Intent(this, camera_placement::class.java)
            startActivity(intent)
            finish()
        }
    }
}


