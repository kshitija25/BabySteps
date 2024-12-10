package com.example.babysteps

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.babysteps.databinding.ActivityWelcomeBinding

class WelcomeUser : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private val splashDuration: Long = 3000 // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Delay using Handler
        Handler().postDelayed(
            {
                // Start the main activity
                startActivity(Intent(this, TermsAndConditions::class.java))

                // Close the splash activity
                finish()
            },
            splashDuration
        )
    }
}