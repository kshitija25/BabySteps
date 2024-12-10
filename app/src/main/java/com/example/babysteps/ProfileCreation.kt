package com.example.babysteps

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ProgressBar
import android.widget.TextView
import com.example.babysteps.databinding.ActivityProfileCreationBinding

class ProfileCreation: AppCompatActivity() {

    private lateinit var binding: ActivityProfileCreationBinding
    private lateinit var progressBar: ProgressBar
    private lateinit var percentageText: TextView
    private var progressStatus = 0
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressBar = findViewById(R.id.progressBar)
        percentageText = findViewById(R.id.percentageText)

        // Start the progress animation
        startProgressAnimation()
    }

    private fun startProgressAnimation() {
        Thread(Runnable {
            while (progressStatus < 100) {
                progressStatus++
                handler.post {
                    binding.progressBar.progress = progressStatus
                    binding.percentageText.text = "$progressStatus%"
                }
                try {
                    // Delay to simulate progress update
                    Thread.sleep(60)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            // Progress reached 100%, start the new activity
            val intent = Intent(this@ProfileCreation, HowManyDaysPregnant::class.java)
            startActivity(intent)
            finish()
        }).start()
    }
}
