package com.example.babysteps

import android.app.AlarmManager
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import java.text.ParseException

class CalendarFragment : Fragment() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var monthSpinner: Spinner
    private lateinit var yearSpinner: Spinner
    private lateinit var calendarGridView: GridView
    private lateinit var daysOfWeekGridView: GridView
    private var remindersMap = mutableMapOf<String, String>()

    private val months = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    private val years = arrayOf("2023", "2024")

    private lateinit var adapter: CalendarGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        // Initialize Firebase references
        firebaseAuth = FirebaseAuth.getInstance()
        val firebaseUser = FirebaseAuth.getInstance().currentUser?.uid
        dbRef = FirebaseDatabase.getInstance().getReference("Users/$firebaseUser")

        // Initialize UI components
        monthSpinner = view.findViewById(R.id.monthSpinner)
        yearSpinner = view.findViewById(R.id.yearSpinner)
        calendarGridView = view.findViewById(R.id.calendarGridView)
        daysOfWeekGridView = view.findViewById(R.id.daysOfWeekGridView)

        // Set up adapters for spinners
        val customMonthSpinnerAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        monthSpinner.adapter = customMonthSpinnerAdapter

        val customYearSpinnerAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        yearSpinner.adapter = customYearSpinnerAdapter

        // Set up spinner listeners
        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedMonth = months[position]
                val selectedYear = years[yearSpinner.selectedItemPosition]
                updateCalendarGrid(selectedMonth, selectedYear)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedMonth = months[monthSpinner.selectedItemPosition]
                val selectedYear = years[position]
                updateCalendarGrid(selectedMonth, selectedYear)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Initialize calendar with current month and year
        val currentMonth = getCurrentMonth()
        val currentYear = getCurrentYear()
        monthSpinner.setSelection(months.indexOf(currentMonth))
        yearSpinner.setSelection(years.indexOf(currentYear))

        // Call readDueDate to fetch and display the due date
        readDueDate()
        createNotificationChannel()
        return view
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "reminder_channel"
            val channelName = "Reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Channel for reminder notifications"
            }

            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = requireContext().getSystemService(AlarmManager::class.java)
                if (alarmManager?.canScheduleExactAlarms() == false) {
                    // Inform the user to manually enable the "Exact Alarm" permission.
                    Toast.makeText(
                        requireContext(),
                        "Please enable 'Exact Alarms' permission for reminders to work.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }



    private fun scheduleNotification(dateKey: String, reminderText: String) {
        val intent = Intent(requireContext(), ReminderBroadcastReceiver::class.java).apply {
            putExtra("reminderText", reminderText)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            dateKey.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        val reminderDate = dateFormat.parse(dateKey) ?: return

        val calendar = Calendar.getInstance().apply {
            time = reminderDate
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 45)
            set(Calendar.SECOND, 0)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enable 'Exact Alarms' permission in settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    private fun updateCalendarGrid(month: String, year: String) {
        val daysOfWeek = arrayOf("S", "M", "T", "W", "T", "F", "S")
        val daysAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, daysOfWeek)
        daysOfWeekGridView.adapter = daysAdapter

        val days = getDaysForMonthYear(month, year)
        adapter = CalendarGridAdapter(requireContext(), days, remindersMap, month, year)
        calendarGridView.adapter = adapter

        fetchRemindersForMonthYear(month, year)
    }

    private fun fetchRemindersForMonthYear(month: String, year: String) {
        dbRef.child("reminders").get().addOnSuccessListener { dataSnapshot ->
            remindersMap.clear()
            for (child in dataSnapshot.children) {
                val reminderDate = child.child("date").value?.toString() ?: continue
                val reminderText = child.child("reminder").value?.toString() ?: ""
                if (reminderDate.contains(month) && reminderDate.contains(year)) {
                    remindersMap[reminderDate] = reminderText
                }
            }
            adapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Log.e("firebase", "Failed to fetch reminders", it)
        }
    }

    private fun getDaysForMonthYear(month: String, year: String): ArrayList<String> {
        val days = ArrayList<String>()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, months.indexOf(month))
        calendar.set(Calendar.YEAR, year.toInt())
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val startingDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        for (i in 1 until startingDayOfWeek) {
            days.add("")
        }

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in 1..daysInMonth) {
            days.add(i.toString())
        }

        return days
    }

    private fun getCurrentMonth(): String {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        return months[month]
    }

    private fun getCurrentYear(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        return year.toString()
    }

    private fun readDueDate() {
        dbRef.child("userProfile").child("dueDate").get().addOnSuccessListener {
            Log.i("firebase", "Got value ${it.value}")
            val dueDateValue = it.value
            displayDueDate(dueDateValue)
        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", it)
        }
    }

    private fun displayDueDate(dueDate: Any?) {
        val dueDateString = dueDate?.toString() ?: "Not Set"
        val estimatedDueDateTextView = view?.findViewById<TextView>(R.id.estDueDate)
        estimatedDueDateTextView?.text = dueDateString
    }

    private inner class CalendarGridAdapter(
        context: Context,
        private val days: ArrayList<String>,
        private val reminders: Map<String, String>,
        private val month: String,
        private val year: String
    ) : ArrayAdapter<String>(context, 0, days) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            val viewHolder: ViewHolder

            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.grid_item_calendar, parent, false)
                viewHolder = ViewHolder()
                viewHolder.dayTextView = view.findViewById(R.id.dayTextView)
                view.tag = viewHolder
            } else {
                viewHolder = view.tag as ViewHolder
            }

            val day = days[position]

            if (day.isNotEmpty()) {
                val dateKey = "$day $month $year"

                if (reminders.containsKey(dateKey)) {
                    viewHolder.dayTextView.setBackgroundResource(R.drawable.circle_background_reminder)
                    viewHolder.dayTextView.setTextColor(Color.WHITE)
                } else {
                    viewHolder.dayTextView.setBackgroundResource(R.drawable.circle_background)
                    viewHolder.dayTextView.setTextColor(Color.BLACK)
                }

                viewHolder.dayTextView.text = day
                viewHolder.dayTextView.setOnClickListener { showReminderDialog(dateKey) }
            } else {
                viewHolder.dayTextView.setBackgroundResource(0)
                viewHolder.dayTextView.text = ""
            }

            return view!!
        }

        private fun showReminderDialog(dateKey: String) {
            // Inflate the updated dialog layout
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_reminder, null)

            val reminderEditText = dialogView.findViewById<EditText>(R.id.reminderEditText)
            val kicksTextView = dialogView.findViewById<TextView>(R.id.kicksTextView)
            val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)

            // Update title dynamically with the selected date
            dialogTitle.text = "Details for $dateKey"

            // Parse date to Firebase format
            val firebaseDateKey = try {
                val inputFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val parsedDate = inputFormat.parse(dateKey)
                outputFormat.format(parsedDate)
            } catch (e: ParseException) {
                Log.e("CalendarFragment", "Error parsing date: $dateKey", e)
                null
            }

            if (firebaseDateKey == null) {
                Toast.makeText(requireContext(), "Invalid date format: $dateKey", Toast.LENGTH_SHORT).show()
                return
            }

            // Fetch existing reminder
            val existingReminder = reminders[dateKey]
            reminderEditText.setText(existingReminder ?: "")

            // Fetch kicks count from Firebase
            dbRef.child("Kicks").child(firebaseDateKey).get().addOnSuccessListener { dataSnapshot ->
                val kicksCount = dataSnapshot.value?.toString()?.toIntOrNull() ?: 0
                kicksTextView.text = "Kicks on this day: $kicksCount"
            }.addOnFailureListener {
                kicksTextView.text = "Kicks on this day: 0"
            }

            // Build and show the dialog
            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
                dialog.dismiss()
            }

            dialogView.findViewById<Button>(R.id.saveButton).setOnClickListener {
                val reminderText = reminderEditText.text.toString()
                saveReminderToFirebase(dateKey, reminderText)
                dialog.dismiss()
            }

            dialog.show()
        }


        private fun saveReminderToFirebase(dateKey: String, reminderText: String) {
            val newReminder = mapOf("date" to dateKey, "reminder" to reminderText)
            dbRef.child("reminders").push().setValue(newReminder).addOnSuccessListener {
                (this@CalendarFragment)?.scheduleNotification(dateKey, reminderText)
            }
        }

        private inner class ViewHolder {
            lateinit var dayTextView: TextView
        }
    }
}