package com.example.babysteps

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.example.babysteps.databinding.ActivityCreateAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CreateAccount : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText:EditText
    private lateinit var createAccountButton: Button
    private lateinit var logInTextView: TextView
    //private lateinit var forgotPasswordTextView: Button
    private lateinit var dbRef: DatabaseReference
    private lateinit var binding: ActivityCreateAccountBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference("Users")

        with(binding) {
            // Add text change listeners to the username and password EditText fields
            createUsernameEditText.addTextChangedListener {
                updateSignUpButtonState()
            }

            createEmailEditText.addTextChangedListener {
                updateSignUpButtonState()
            }

            createPasswordEditText.addTextChangedListener {
                updateSignUpButtonState()
            }

            confirmPasswordEditText.addTextChangedListener { text ->
                updateSignUpButtonState()
            }

            createAccountButton.setOnClickListener {
                val username = createUsernameEditText.text.toString()
                val email = createEmailEditText.text.toString()
                val password = createPasswordEditText.text.toString()
                val confirmPassword = confirmPasswordEditText.text.toString()

                // Perform sign-up logic here
                if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                    if (password == confirmPassword) {
                        firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    // Redirect to the desired activity
                                    val intent = Intent(this@CreateAccount, MomExperience::class.java)
                                    saveUserEmailPassword(email, username)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@CreateAccount,
                                        it.exception.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(this@CreateAccount, "The password does not match!", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(this@CreateAccount, "Please fill the fields", Toast.LENGTH_SHORT).show()
                }
            }

            logInTextView.setOnClickListener {
                val intent = Intent(this@CreateAccount, LoginAccount::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
    private fun saveUserEmailPassword(userEmail: String, userName: String){
        val firebaseUser = firebaseAuth.currentUser?.uid
        val user = userMommyModel(
            loginCredentials = userMommyModel.LoginCredentials(
                email = userEmail
            ),
            userProfile = userMommyModel.UserProfile(
                userId = firebaseUser,
                userName = userName
            )
        )

        if (firebaseUser != null) {
            dbRef.child(firebaseUser).setValue(user)
                .addOnCompleteListener {
                    Toast.makeText(this,"data stored successfully", Toast.LENGTH_LONG).show()
                }
        }
    }


    private fun updateSignUpButtonState() {
        val isUsernameFilled = binding.createUsernameEditText.text.isNotEmpty()
        val isEmailFilled = binding.createEmailEditText.text.isNotEmpty()
        val isPasswordFilled = binding.createPasswordEditText.text.isNotEmpty()
        val isConfirmPasswordFilled = binding.confirmPasswordEditText.text.isNotEmpty()
        val isPasswordMatched = binding.createPasswordEditText.text.toString() == binding.confirmPasswordEditText.text.toString()

        binding.createAccountButton.isEnabled = isUsernameFilled && isEmailFilled && isPasswordFilled && isConfirmPasswordFilled && isPasswordMatched
        if (binding.createAccountButton.isEnabled) {
            binding.createAccountButton.setBackgroundResource(R.drawable.log_button)
            binding.createAccountButton.setTextColor(Color.parseColor("#FE5065"))
        } else {
            binding.createAccountButton.setBackgroundResource(R.drawable.disable_button)
            binding.createAccountButton.setTextColor(Color.parseColor("#D2D1D1"))
        }


        binding.passwordMismatchTextView.isVisible = !isPasswordMatched

        binding.confirmPasswordEditText.isEnabled = isPasswordFilled
        if (binding.confirmPasswordEditText.isEnabled) {
            binding.confirmPasswordEditText.setBackgroundResource(R.drawable.custom_edit_text)
            binding.confirmPasswordEditText.setTextColor(Color.parseColor("#FF000000"))
        } else {
            binding.confirmPasswordEditText.setBackgroundResource(R.drawable.custom_edit_text_disabled)
            binding.confirmPasswordEditText.setTextColor(Color.parseColor("#D2D1D1"))
        }

        binding.showPasswordToggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Show password
                binding.createPasswordEditText.transformationMethod = null
            } else {
                // Hide password
                binding.createPasswordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }

        binding.confirmShowPasswordToggleButton.isEnabled = isConfirmPasswordFilled
        binding.confirmShowPasswordToggleButton.setOnCheckedChangeListener{ _, isChecked ->
            if (isChecked) {
                // Show password
                binding.confirmPasswordEditText.transformationMethod = null
            } else {
                // Hide password
                binding.confirmPasswordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }

        // Set cursor position to the end of the password field
        binding.createPasswordEditText.setSelection(binding.createPasswordEditText.text.length)
        binding.confirmPasswordEditText.setSelection(binding.confirmPasswordEditText.text.length)
    }
}