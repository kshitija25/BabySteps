package com.example.babysteps

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.babysteps.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var dbRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storageReference: StorageReference
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)

        // Handle edit icon click for profile picture
        binding.editProfilePicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        val emergencyContactInput = binding.emergencyContactInput
        val saveButton = binding.saveEmergencyContactButton
        saveButton.setOnClickListener {
            val emergencyContact = emergencyContactInput.text.toString()

            if (emergencyContact.isNotEmpty()) {
                val firebaseUser = FirebaseAuth.getInstance().currentUser?.uid
                val dbRef = FirebaseDatabase.getInstance().getReference("Users/$firebaseUser")

                dbRef.child("emergencyContact").setValue(emergencyContact)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Emergency contact saved!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to save contact. Try again.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), "Please enter a valid contact number.", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        val firebaseUser = FirebaseAuth.getInstance().currentUser?.uid
        dbRef = FirebaseDatabase.getInstance().getReference("Users/$firebaseUser")
        storageReference = FirebaseStorage.getInstance().reference

        readLoginCredentials()
        loadProfilePicture()
        loadEmergencyContact()
    }

    private fun readLoginCredentials() {
        dbRef.child("loginCredentials").child("email").get().addOnSuccessListener {
            Log.i("firebase", "Got value ${it.value}")
            val value = it.value
            binding.userEmail.text = value.toString()
        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", it)
        }
    }

    private fun loadProfilePicture() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val fileRef = storageReference.child("users/$firebaseUser/profile.jpg")

        fileRef.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).into(binding.profilePicture)
        }.addOnFailureListener {
            Log.e("firebase", "Failed to load profile picture", it)
        }
    }

    private fun uploadImageToFirebase() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val fileRef = storageReference.child("users/$firebaseUser/profile.jpg")

        if (imageUri != null) {
            fileRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        saveProfilePictureUriToDatabase(uri.toString())
                        loadProfilePicture()
                        Toast.makeText(context, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to upload picture.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "No image selected.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveProfilePictureUriToDatabase(uri: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser?.uid ?: return
        dbRef.child("userProfile").child("profilePictureUri").setValue(uri)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            uploadImageToFirebase()
        }
    }

    private fun loadEmergencyContact() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser?.uid
        val dbRef = FirebaseDatabase.getInstance().getReference("Users/$firebaseUser")

        dbRef.child("emergencyContact").get().addOnSuccessListener {
            val emergencyContact = it.value?.toString() ?: ""
            if (emergencyContact.isNotEmpty()) {
                binding.emergencyContactInput.setText(emergencyContact)
            } else {
                binding.emergencyContactInput.hint = "Enter emergency contact"
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to load contact.", Toast.LENGTH_SHORT).show()
        }
    }
}
