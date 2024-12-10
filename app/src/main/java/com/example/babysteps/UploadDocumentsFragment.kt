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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.babysteps.adapters.ImagesAdapter
import com.example.babysteps.databinding.FragmentUploadDocumentsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UploadDocumentsFragment : Fragment() {

    private lateinit var binding: FragmentUploadDocumentsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storageReference: StorageReference
    private lateinit var dbRef: DatabaseReference
    private val imageUrls = mutableListOf<String>() // List to store image URLs
    private val PICK_IMAGE_REQUEST_CODE = 101

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUploadDocumentsBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        dbRef = FirebaseDatabase.getInstance().reference

        setupRecyclerView()
        setupUploadButton()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.documentsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3) // 3 columns
        binding.documentsRecyclerView.adapter = ImagesAdapter(imageUrls) { imageUrl ->
            openImage(imageUrl) // Handle image click
        }
        fetchUploadedImages()
    }
    private fun openImage(imageUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(imageUrl), "image/*") // Set image URI and type
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No app available to open the image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUploadButton() {
        binding.fabUploadDocument.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*" // Only allow image selection
            startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
        }
    }

    private fun fetchUploadedImages() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        dbRef.child("Users/$userId/images").get().addOnSuccessListener { snapshot ->
            imageUrls.clear()
            snapshot.children.forEach {
                val imageUrl = it.value.toString()
                imageUrls.add(imageUrl)
            }
            binding.documentsRecyclerView.adapter?.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to fetch images", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            if (imageUri != null) {
                uploadImageToFirebase(imageUri)
            } else {
                Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Log.e("UploadDocumentsFragment", "User not authenticated. Cannot upload image.")
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val fileRef = storageReference.child("images/$userId/${System.currentTimeMillis()}.jpg")
        Log.d("UploadDocumentsFragment", "Uploading image to path: ${fileRef.path}")

        fileRef.putFile(imageUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    Log.d("UploadDocumentsFragment", "Image uploaded successfully: $uri")
                    saveImageUrlToDatabase(uri.toString())
                    imageUrls.add(uri.toString()) // Add to list and update UI
                    binding.documentsRecyclerView.adapter?.notifyDataSetChanged()
                    Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Log.e("UploadDocumentsFragment", "Image upload failed: ${it.message}")
                Toast.makeText(requireContext(), "Image upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun saveImageUrlToDatabase(imageUrl: String) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val imageId = dbRef.child("Users/$userId/images").push().key ?: return
        dbRef.child("Users/$userId/images/$imageId").setValue(imageUrl)
    }
}
