package com.example.babysteps

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.babysteps.databinding.ActivityUserDueDateBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UserDueDate : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityUserDueDateBinding
    private lateinit var dayPicker: NumberPicker
    private lateinit var monthPicker: NumberPicker
    private lateinit var yearPicker: NumberPicker
    private lateinit var btnSelectDate: Button
    private lateinit var idkBtn: Button

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDueDateBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        val firebaseUser = FirebaseAuth.getInstance().currentUser?.uid
        dbRef = FirebaseDatabase.getInstance().getReference("Users/$firebaseUser")
        setContentView(binding.root)

        dayPicker = findViewById(R.id.dayPicker)
        monthPicker = findViewById(R.id.monthPicker)
        yearPicker = findViewById(R.id.yearPicker)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        idkBtn = findViewById(R.id.idkBtn)

        with(binding) {
            // Set the minimum and maximum values for the pickers
            val currentYear = getCurrentYear()
            dayPicker.minValue = 1
            dayPicker.maxValue = 31
            monthPicker.minValue = 1
            monthPicker.maxValue = 12
            yearPicker.minValue = currentYear
            yearPicker.maxValue = currentYear + 1

            // Set a custom formatter for the month picker
            val monthNames = arrayOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
            monthPicker.displayedValues = monthNames

            // Set a scroll listener for the pickers
            val pickerScrollListener = NumberPicker.OnScrollListener { _, _ ->
                btnSelectDate.isEnabled = true
                btnSelectDate.setBackgroundResource(R.drawable.gradient_bg)
            }
            dayPicker.setOnScrollListener(pickerScrollListener)
            monthPicker.setOnScrollListener(pickerScrollListener)
            yearPicker.setOnScrollListener(pickerScrollListener)

            dayPicker.textSize = 25f
            monthPicker.textSize = 25f
            yearPicker.textSize = 25f

            // Disable the button initially
            btnSelectDate.isEnabled = false
            btnSelectDate.setBackgroundResource(R.drawable.disabled_btn)

            // Set a click listener for the "Next" button
            btnSelectDate.setOnClickListener {
                val selectedDay = dayPicker.value
                val selectedMonth = monthPicker.value
                val selectedYear = yearPicker.value

                val dueDate = "${monthNames[selectedMonth - 1]} $selectedDay $selectedYear"
                saveDueDate(dueDate)
                readDueDate()

                val intent = Intent(this@UserDueDate, HowManyDaysPregnant::class.java)
                intent.putExtra("due_date", dueDate)
                startActivity(intent)
                finish()

                Toast.makeText(this@UserDueDate, "Selected Due Date: $dueDate", Toast.LENGTH_SHORT).show()
                // Add your desired logic for handling the selected due date here
            }

            idkBtn.setOnClickListener {
                val intent = Intent(this@UserDueDate, DueDateCalculator::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun readDueDate() {
        dbRef.child("userProfile").child("dueDate").get().addOnSuccessListener {
            Log.i("firebase", "Got value ${it.value}")
            val dueDate = it.value
            displayDueDate(dueDate.toString())
        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", it)
        }
    }

    private fun displayDueDate(dueDate: Any?) {
        val dueDateString = dueDate.toString()
        val parts = dueDateString.split(" ")
        if (parts.size >= 3) {
            val day = parts[1]
            val month = parts[0]
            val year = parts[2]

            val formattedDueDate = "$month $day, $year"

            println(formattedDueDate)
        }
    }

    private fun saveDueDate(dueDate: String) {
        val newKeyValuePair = HashMap<String, Any>()
        newKeyValuePair["dueDate"] = dueDate

        dbRef.child("userProfile").updateChildren(newKeyValuePair)
            .addOnCompleteListener {
                Toast.makeText(this, "Data stored successfully", Toast.LENGTH_LONG).show()
            }
    }

    private fun getCurrentYear(): Int {
        val calendar = java.util.Calendar.getInstance()
        return calendar.get(java.util.Calendar.YEAR)
    }
}
