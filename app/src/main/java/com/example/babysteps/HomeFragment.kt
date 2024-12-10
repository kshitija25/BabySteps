package com.example.babysteps

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.babysteps.databinding.FragmentHomeBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class HomeFragment : Fragment() {
    private lateinit var dbRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: FragmentHomeBinding

    private val shimmerDuration = 2000L // Duration of shimmer animation in milliseconds

    var babyAgeInDays: Long = 0
    var babyAgeInWeeks: Long = 0
    var babyAgeInMonths: Long = 0

    private var isFragmentAttached: Boolean = false
    override fun onAttach(context: Context) {
        super.onAttach(context)
        isFragmentAttached = true

        // Move the code that requires context to onAttach() method
        if (isConnectedToInternet()) {
            firebaseAuth = FirebaseAuth.getInstance()
            val firebaseUser = firebaseAuth.currentUser?.uid
            dbRef = FirebaseDatabase.getInstance().reference

            readTimeStamp(firebaseUser)
        } else {
            showNoInternetDialog()
        }

    }
    override fun onDetach() {
        super.onDetach()
        isFragmentAttached = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    private fun isConnectedToInternet(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            ?: false
    }

    private fun showNoInternetDialog() {
        val dialogView = layoutInflater.inflate(R.layout.custom_dialog_no_internet, null)
        val dialogTitleTextView = dialogView.findViewById<TextView>(R.id.dialogTitleTextView)
        val dialogMessageTextView = dialogView.findViewById<TextView>(R.id.dialogMessageTextView)
        val retryButton = dialogView.findViewById<Button>(R.id.retryButton)
        val exitButton = dialogView.findViewById<Button>(R.id.exitButton)

        dialogTitleTextView.text = "No Internet Connection"
        dialogMessageTextView.text = "Please check your internet connection and try again."

        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)

        val dialog = dialogBuilder.create()
        dialog.show()

        // Apply the custom button background programmatically
        retryButton.setBackgroundResource(R.drawable.gradient_bg)
        exitButton.setBackgroundResource(R.drawable.gradient_bg)

        // Customize other button attributes as needed
        retryButton.setTextColor(resources.getColor(R.color.white))
        exitButton.setTextColor(resources.getColor(R.color.white))

        // Set click listeners for the buttons
        retryButton.setOnClickListener {
            if (isConnectedToInternet()) {
                // Internet connection is available, perform the necessary actions
                firebaseAuth = FirebaseAuth.getInstance()
                val firebaseUser = firebaseAuth.currentUser?.uid
                dbRef = FirebaseDatabase.getInstance().getReference("Users/$firebaseUser")

                readTimeStamp(firebaseUser)
            } else {
                // Internet connection is still not available, show the dialog again
                showNoInternetDialog()
            }
            dialog.dismiss()
        }

        exitButton.setOnClickListener {
            // Exit the activity or perform any other action
            activity?.finish()
            dialog.dismiss()
        }
    }

    private fun readDueDate(firebaseUser: String?) {
        dbRef.child("Users/$firebaseUser").child("userProfile").child("dueDate").get().addOnSuccessListener { snapshot ->
            val dueDate = snapshot.value as? String
            displayRemainingDate(dueDate)
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

    private fun displayRemainingDate(dueDate: String?) {
        dueDate?.let {
            val dateFormat = SimpleDateFormat("MMMM dd yyyy", Locale.getDefault())
            val parseDueDate = dateFormat.parse(dueDate)
            if (parseDueDate != null) {
                val remainingDays = calculateRemainingDays(parseDueDate)
                val remainingDaysString = remainingDays.toString()
                val remainDaysTextView = "Remaining days: $remainingDaysString"
                binding.remainingDaysTextView.text = remainDaysTextView

                // Show the content views when the data is ready
                showContent()
            } else {
                // Handle the case of an invalid due date format
            }
        } ?: println("Due date not found.")
    }

    private fun displayDayN(timeStamp: String?, dueDate: String?) {
        val dateFormat = SimpleDateFormat("MMMM dd yyyy", Locale.getDefault())
        val parseTimeStamp = timeStamp?.let { dateFormat.parse(it) }
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

            displayBabyGrowthImage(ageInWeeks.toInt())
            displayBabyGrowthTxt(ageInWeeks.toInt())
            displayMomHealthTxt(ageInWeeks.toInt())

            val dayNString = "Week $ageInWeeks, Day $ageInDays"
            binding.dayNumberTextView.text = dayNString

            Log.d("BabySteps", "Calculated weeks: $ageInWeeks, days: $ageInDays")
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

    private fun readTimeStamp(firebaseUser: String?) {
        dbRef.child("Users/$firebaseUser").child("userProfile").child("firstDayTimeStamp").get().addOnSuccessListener { snapshot ->
            val timeStamp = snapshot.value as? String
            if (timeStamp != null) {
                timeStampChecker(true, timeStamp, firebaseUser)
                readDueDate(firebaseUser)
                // Call readDueDate() first to get the dueDate value
                dbRef.child("Users/$firebaseUser").child("userProfile").child("dueDate").get().addOnSuccessListener { snapshot ->
                    val dueDate = snapshot.value as? String
                    displayDayN(timeStamp, dueDate)
                }
            } else {
                timeStampChecker(false, null, firebaseUser)
            }
        }.addOnFailureListener { exception ->
            // Handle the failure case
        }
    }

    private fun timeStampChecker(value: Boolean, timeStamp: String?, firebaseUser: String?) {
        if (value) {
            println("timestamp already exists: $timeStamp")
        } else {
            println("timestamp added")
            saveTimeStamp(firebaseUser)
        }
    }

    private fun saveTimeStamp(firebaseUser: String?) {
        val currentDate = SimpleDateFormat("MMMM dd yyyy", Locale.getDefault()).format(Date())
        val newKeyValuePair = HashMap<String, Any>()
        newKeyValuePair["firstDayTimeStamp"] = currentDate
        dbRef.child("Users/$firebaseUser").child("userProfile").updateChildren(newKeyValuePair)
    }

    private fun startShimmerAnimation() {
        binding.shimmerLayout.startShimmer()
        binding.shimmerLayout.tag = System.currentTimeMillis()
    }

    private fun stopShimmerAnimation() {
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.visibility = View.GONE
    }

    private fun showContent() {
        binding.contentLayout.visibility = View.VISIBLE
    }

    private fun hideContent() {
        binding.contentLayout.visibility = View.INVISIBLE
    }

    private fun simulateDataLoading() {
        val shimmerStartTime = binding.shimmerLayout.tag as? Long ?: 0L
        val elapsedTime = System.currentTimeMillis() - shimmerStartTime
        val remainingTime = shimmerDuration - elapsedTime

        // Delay the display of content by 0.005 seconds (500 milliseconds)
        val delayDuration = 1000L

        if (remainingTime <= delayDuration) {
            stopShimmerAnimation()
            showContent()
        } else {
            // Simulate data loading delay
            binding.root.postDelayed({
                stopShimmerAnimation()
                showContent()
            }, delayDuration)
        }
    }

    private fun displayBabyGrowthImage(ageInWeeks: Int) {
        if (!isFragmentAttached) return

        val imageResourceId = "baby_growth_week_${ageInWeeks}"

        // Fetch resource dynamically
        val actualResourceId = this@HomeFragment.resources.getIdentifier(
            imageResourceId,
            "drawable",
            requireContext().packageName
        )

        if (actualResourceId != 0) { // Ensure the resource exists
            binding.babyGrowthImageView.setImageResource(actualResourceId)
            Log.d("BabySteps", "Setting image resource for week $ageInWeeks: $actualResourceId")
        } else {
            Log.e("BabySteps", "Resource not found for week $ageInWeeks. Using default image.")
            binding.babyGrowthImageView.setImageResource(
                this@HomeFragment.resources.getIdentifier(
                    "baby_growth_week_1n6days",
                    "drawable",
                    requireContext().packageName
                )
            )
        }
    }


    private fun displayBabyGrowthTxt(ageInWeeks: Int) {
        val babyGrowthStr = when (ageInWeeks) {
            in 2..40 -> "baby_growth_week_${ageInWeeks}"
            else -> "baby_growth_week_1n6days"
        }
        dbRef.child("Description").child(babyGrowthStr).child("babyGrowth").get()
            .addOnSuccessListener { snapshot ->
                val babyGrowth = snapshot.value as? String
                if (babyGrowth != null) {
                    binding.babyGrowthTextView.text = babyGrowth
                    println("Fetched Baby Growth: $babyGrowth") // Debug log
                } else {
                    println("No babyGrowth data found for $babyGrowthStr") // Debug log
                }
            }
            .addOnFailureListener { exception ->
                println("Failed to fetch babyGrowth: ${exception.message}") // Debug log
            }
    }

    private fun displayMomHealthTxt(ageInWeeks: Int) {
        val momHealthStr = when (ageInWeeks) {
            in 2..40 -> "baby_growth_week_${ageInWeeks}"
            else -> "baby_growth_week_1n6days"
        }
        dbRef.child("Description").child(momHealthStr).child("momHealth").get()
            .addOnSuccessListener { snapshot ->
                val momHealth = snapshot.value as? String
                if (momHealth != null) {
                    binding.momHealthTextView.text = momHealth
                    println("Fetched Mom Health: $momHealth") // Debug log
                } else {
                    println("No momHealth data found for $momHealthStr") // Debug log
                }
            }
            .addOnFailureListener { exception ->
                println("Failed to fetch momHealth: ${exception.message}") // Debug log
            }
    }


    private fun showFullTextDialog(fullText: String?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_full_text, null)
        val fullTextTextView = dialogView.findViewById<TextView>(R.id.fullTextTextView)

        fullTextTextView.text = fullText

        val dialogBuilder = AlertDialog.Builder(requireContext(), R.style.CustomDialogStyle2)
            .setView(dialogView)

        val dialog = dialogBuilder.create()
        dialog.show()

        // Set click listener for the close button
        val closeButton = dialogView.findViewById<ImageView>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
        }
    }
    private fun getCurrentDateAsString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun incrementKickCount(date: String) {
        val firebaseUser = firebaseAuth.currentUser?.uid
        if (firebaseUser != null) {
            val kicksRef = dbRef.child("Users/$firebaseUser/Kicks/$date")

            kicksRef.get().addOnSuccessListener { snapshot ->
                val currentCount = snapshot.getValue(Int::class.java) ?: 0
                val updatedCount = currentCount + 1

                kicksRef.setValue(updatedCount).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Kick count updated: $updatedCount", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to update kick count", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to retrieve kick count", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide the content views initially
        hideContent()

        // Start shimmer animation
        startShimmerAnimation()

        // Simulate data loading delay and stop shimmer animation
        simulateDataLoading()

        val fabKickTracker: FloatingActionButton = binding.root.findViewById(R.id.fab_kick_tracker)

        fabKickTracker.setOnClickListener {
            val currentDate = getCurrentDateAsString()
            incrementKickCount(currentDate)
        }
    }
}

