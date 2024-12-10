package com.example.babysteps

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.babysteps.databinding.ActivityBirthYearPickerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.time.Year

class BirthYearPicker : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityBirthYearPickerBinding

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBirthYearPickerBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        val firebaseUser = FirebaseAuth.getInstance().currentUser?.uid
        dbRef = FirebaseDatabase.getInstance().getReference("Users/$firebaseUser")
        setContentView(binding.root)

        with(binding) {
            btnSelectYear.isEnabled = false
            val currentYear = Year.now().value
            println(currentYear)
            val startYear = 1980
            val endYear = currentYear - 18

            npYear.minValue = 0
            npYear.maxValue = endYear - startYear + 1
            npYear.displayedValues = buildDisplayValues(startYear, endYear)
            npYear.value = npYear.maxValue / 2 // Set initial value to the middle index
            npYear.wrapSelectorWheel = false
            npYear.textSize = 40f

            npYear.setOnScrollListener { _, _ ->
                // Enable the Next button when the NumberPicker is scrolled
                updateButtonEnabledState()
            }
            updateButtonEnabledState()

            btnSelectYear.setOnClickListener {
                // Get the selected year from the NumberPicker
                //val selectedYear = if (npYear.value == 0) null else yearRange[npYear.value].toInt()

                // Update the selected year in the TextView
                //tvSelectedYear.text = "Selected Year: ${selectedYear ?: "Not selected"}"
                val birthYear = binding.npYear.displayedValues[binding.npYear.value]
                saveUserBirthYear(birthYear)
                readUserBirthYear()
                val intent = Intent(this@BirthYearPicker, UserDueDate::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun readUserBirthYear(){
        dbRef.child("userProfile").child("birthYear").get().addOnSuccessListener {
            Log.i("firebase", "Got value ${it.value}")
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }

    }
    private fun saveUserBirthYear(birthYear: String) {
        val newKeyValuePair = HashMap<String, Any>()
        newKeyValuePair["birthYear"] = birthYear
        dbRef.child("userProfile").updateChildren(newKeyValuePair)
            .addOnCompleteListener{ Toast.makeText(this,"data stored sucessfully", Toast.LENGTH_LONG).show()
            }
    }

    private fun buildDisplayValues(startYear: Int, endYear: Int): Array<String> {
        val yearRange = ArrayList<String>()
        val selectOption = "Select"

        for (year in startYear..endYear) {
            yearRange.add(year.toString())
        }

        val middleIndex = yearRange.size / 2
        yearRange.add(middleIndex, selectOption)

        return yearRange.toTypedArray()
    }

    private fun updateButtonEnabledState() {
        val selectedValue = binding.npYear.displayedValues[binding.npYear.value]
        if (selectedValue == "Select") {
            // Disable the button and set the background color for disabled state
            binding.btnSelectYear.isEnabled = false
            binding.btnSelectYear.setBackgroundResource(R.color.disabled_btn)
        } else {
            // Enable the button and set the background color for enabled state
            binding.btnSelectYear.isEnabled = true
            binding.btnSelectYear.setBackgroundResource(R.drawable.gradient_bg)
        }
    }
}