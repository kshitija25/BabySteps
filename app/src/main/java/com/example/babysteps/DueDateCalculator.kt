package com.example.babysteps

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DueDateCalculator : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var calendarView: CalendarView
    private lateinit var btnSelectDate: Button

    private var selectedDate: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        val firebaseUser = firebaseAuth.currentUser?.uid
        dbRef = FirebaseDatabase.getInstance().getReference("Users/$firebaseUser")
        setContentView(R.layout.activity_due_date_calculator)

        // Initialize views
        calendarView = findViewById(R.id.calendarView)
        btnSelectDate = findViewById(R.id.btnSelectDate)

        // Set initial button state
        btnSelectDate.isEnabled = false

        // Set listeners
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Convert selected date to milliseconds
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.timeInMillis

            // Enable button
            btnSelectDate.isEnabled = true
            btnSelectDate.setBackgroundResource(R.drawable.gradient_bg)
        }

        btnSelectDate.setOnClickListener {
            // Calculate due date based on the selected date
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.timeInMillis = selectedDate
            val dueDate = calculateDueDate(selectedCalendar.time)

            // Display the due date in a toast message
            val dateFormat = SimpleDateFormat("MMMM dd yyyy", Locale.getDefault())
            val formattedDueDate = dateFormat.format(dueDate)
            val message = "Estimated due date: $formattedDueDate"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

            saveDueDate(formattedDueDate)
            readDueDate()
            val intent = Intent(this, HowManyDaysPregnant::class.java)
            intent.putExtra("due_date", formattedDueDate) // Pass the formatted due date
            startActivity(intent)
            finish()
        }
    }

    private fun readDueDate() {
        dbRef.child("userProfile").child("dueDate").get().addOnSuccessListener {
            Log.i("firebase", "Got value ${it.value}")
            val dueDate = it.value as? String
        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", it)
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

    private fun calculateDueDate(lastMenstruationDate: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = lastMenstruationDate

        // Add seven days
        calendar.add(Calendar.DAY_OF_MONTH, 7)

        // Subtract three months
        calendar.add(Calendar.MONTH, -3)

        // Check if the resulting date is in the past
        val currentDate = Calendar.getInstance()
        if (calendar.before(currentDate)) {
            // Increment the year by 1
            calendar.add(Calendar.YEAR, 1)
        }

        return calendar.time
    }
}