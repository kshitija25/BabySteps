package com.example.babysteps

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.babysteps.databinding.ActivityMomExperienceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MomExperience : AppCompatActivity() {
    private lateinit var binding: ActivityMomExperienceBinding
    private lateinit var dbRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMomExperienceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        val firebaseUser = firebaseAuth.currentUser?.uid
        dbRef = FirebaseDatabase.getInstance().getReference("Users/$firebaseUser")

        with(binding) {
            yesButton.setOnClickListener {
                saveFirstTimeMomData(true) // Save "true" for first-time mom
                navigateToNextScreen()
            }

            noButton.setOnClickListener {
                saveFirstTimeMomData(false) // Save "false" for not a first-time mom
                navigateToNextScreen()
            }
        }
    }

    private fun saveFirstTimeMomData(isFirstTimeMom: Boolean) {
        val newKeyValuePair = HashMap<String, Any>()
        newKeyValuePair["isFirstTimeMom"] = isFirstTimeMom
        dbRef.child("userProfile").updateChildren(newKeyValuePair)
            .addOnCompleteListener {
                // Log or display success message if needed
            }
            .addOnFailureListener {
                // Handle error
            }
    }

    private fun navigateToNextScreen() {
        val intent = Intent(this@MomExperience, BirthYearPicker::class.java)
        startActivity(intent)
        finish()
    }
}
