package com.example.babysteps

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.example.babysteps.databinding.ActivityLoginAccountBinding
import com.google.firebase.auth.FirebaseAuth

class LoginAccount : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityLoginAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        with(binding) {
            loginButton.isEnabled = false // Initially disable the loginButton

            // Add text change listeners to the username and password EditText fields
            loginUsernameEditText.addTextChangedListener { text ->
                updateLoginButtonState()
            }

            loginPasswordEditText.addTextChangedListener { text ->
                updateLoginButtonState()
            }

            loginButton.setOnClickListener {
                val username = loginUsernameEditText.text.toString()
                val password = loginPasswordEditText.text.toString()

                // Perform login logic here
                if (username.isNotEmpty() && password.isNotEmpty()) {
                    firebaseAuth.signInWithEmailAndPassword(username, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Save login status in shared preferences
                                val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                                val editor = sharedPrefs.edit()
                                editor.putBoolean("isLoggedIn", true)
                                editor.apply()

                                // Redirect to the homepage activity
                                val intent = Intent(this@LoginAccount, Homepage::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@LoginAccount, task.exception?.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this@LoginAccount, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }

            createAccountTextView.setOnClickListener {
                val intent = Intent(this@LoginAccount, CreateAccount::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun updateLoginButtonState() {
        val isUsernameFilled = binding.loginUsernameEditText.text.isNotEmpty()
        val isPasswordFilled = binding.loginPasswordEditText.text.isNotEmpty()
        binding.loginButton.isEnabled = isUsernameFilled && isPasswordFilled

        if (binding.loginButton.isEnabled) {
            binding.loginButton.setBackgroundResource(R.drawable.log_button)
            binding.loginButton.setTextColor(Color.parseColor("#FE5065"))
        } else {
            binding.loginButton.setBackgroundResource(R.drawable.disable_button)
            binding.loginButton.setTextColor(Color.parseColor("#D2D1D1"))
        }

        binding.showPasswordToggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Show password
                binding.loginPasswordEditText.transformationMethod = null
            } else {
                // Hide password
                binding.loginPasswordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }
        // Set cursor position to the end of the password field
        binding.loginPasswordEditText.setSelection(binding.loginPasswordEditText.text.length)
    }
}
