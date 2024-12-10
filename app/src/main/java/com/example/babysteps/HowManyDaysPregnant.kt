package com.example.babysteps

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.babysteps.databinding.ActivityHowManyDaysPregnantBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HowManyDaysPregnant : AppCompatActivity() {
    private lateinit var btnContinue: Button
    private lateinit var dbRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityHowManyDaysPregnantBinding

    var babyAgeInDays: Long = 0
    var babyAgeInWeeks: Long = 0
    var babyAgeInMonths: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_how_many_days_pregnant)
        firebaseAuth = FirebaseAuth.getInstance()
        val firebaseUser = firebaseAuth.currentUser?.uid
        dbRef = FirebaseDatabase.getInstance().reference
        binding = ActivityHowManyDaysPregnantBinding.inflate(layoutInflater)
        setContentView(binding.root)
        readDueDate(firebaseUser)



        btnContinue = findViewById(R.id.continueButton)
        btnContinue.setOnClickListener {
            val intent = Intent(this, OfficialWelcome::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun readDueDate(firebaseUser: String?) {
        dbRef.child("Users/$firebaseUser").child("userProfile").child("dueDate").get().addOnSuccessListener { snapshot ->
            val dueDate = snapshot.value as? String
            displayDayN(dueDate)
        }.addOnFailureListener { exception ->
            // Handle the failure case
        }
    }

    private fun getCurrentDate(): Long {
        val currentDate = Calendar.getInstance().time
        return currentDate.time
    }

    private fun calculateRemainingDays(dueDate: Date): Long {
        val currentTime = getCurrentDate()
        val dueTime = dueDate.time
        val remainingTime = dueTime - currentTime
        return remainingTime / (1000 * 60 * 60 * 24)
    }



    private fun displayDayN( dueDate: String?) {
        val currentDate = SimpleDateFormat("MMMM dd yyyy", Locale.getDefault()).format(Date())
        val dateFormat = SimpleDateFormat("MMMM dd yyyy", Locale.getDefault())
        val parseTimeStamp = currentDate.let { dateFormat.parse(it) }
        val parseDueDate = dueDate?.let { dateFormat.parse(it) }

        if (parseTimeStamp != null && parseDueDate != null) {
            val dayN = calculateDayN(parseTimeStamp).toString()
            val remainingDays = calculateRemainingDays(parseDueDate)
            val differenceDays = 280 - remainingDays


            val (weeks, days) = calculateWeeksAndDays(dayN.toLong())



            var ageInWeeks = weeks + differenceDays / 7
            var ageInDays = days + differenceDays % 7


            if (ageInDays < 0) {
                ageInWeeks--
                ageInDays += 7
            }

            // Update public variables with the calculated values
            babyAgeInWeeks = ageInWeeks
            babyAgeInDays = ageInDays
            babyAgeInMonths = calculateMonths(ageInWeeks)


            val dayNString = "You are $ageInWeeks weeks and $ageInDays days pregnant!"
            binding.welcomeMessage.text = dayNString
        } else {
            // Handle the case of invalid date format
        }
    }



    // Calculate baby's age in months based on weeks
    private fun calculateMonths(weeks: Long): Long {
        return weeks / 4 // Assuming one month has 4 weeks
    }

    private fun calculateWeeksAndDays(dayN: Long): Pair<Long, Long> {
        val weeks = dayN / 7
        val remainingDays = dayN % 7
        return Pair(weeks, remainingDays)
    }

    private fun calculateDayN(timeStamp: Date): Long {
        val currentTime = getCurrentDate()
        val timeStampDate = timeStamp.time
        val dayNTime = currentTime - timeStampDate + (1000 * 60 * 60 * 24)
        return dayNTime / (1000 * 60 * 60 * 24)
    }








}