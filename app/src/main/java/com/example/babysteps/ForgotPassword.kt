package com.example.babysteps

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.babysteps.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : ComponentActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        with(binding) {
            resetPasswordButton.setOnClickListener() {
                val sPassword = resetEmailEditText.text.toString()
                auth.sendPasswordResetEmail(sPassword)
                    .addOnCompleteListener {
                        Toast.makeText(this@ForgotPassword, "Please check your email", Toast.LENGTH_SHORT).show()
                    }
                    .addOnCompleteListener {
                        Toast.makeText(this@ForgotPassword, it.toString(), Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
