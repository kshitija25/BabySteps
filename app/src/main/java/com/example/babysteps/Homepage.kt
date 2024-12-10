package com.example.babysteps

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.babysteps.databinding.ActivityHomepageBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Homepage : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var binding: ActivityHomepageBinding
    private var backPressedOnce = false
    private var backPressedCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        binding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(HomeFragment())
        val openCalendarTab = intent.getBooleanExtra("open_calendar_tab", false)
        if (openCalendarTab) {
            replaceFragment(CalendarFragment()) // Switch to Calendar tab
            binding.bottomNavigationView.selectedItemId = R.id.calendar // Highlight Calendar tab
        }
        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.calendar -> replaceFragment(CalendarFragment())
                R.id.nearest_hospital -> replaceFragment(NearestHospitalFragment())
                R.id.tipsnadvice -> replaceFragment(TipsAndAdviceFragment())
                else -> {}
            }
            true
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)

        drawerLayout = findViewById(R.id.drawer_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val toolbarIcon = findViewById<ImageView>(R.id.toolbar_icon)
        toolbarIcon.setOnClickListener {
            fetchEmergencyContactAndCall()
        }

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.visibility = View.VISIBLE
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        for (i in 0 until bottomNavigationView.menu.size()) {
            val menuItem = bottomNavigationView.menu.getItem(i)
            menuItem.title = ""
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if (backPressedCount == 1) {
                super.onBackPressed()
            } else {
                backPressedCount++

                val toast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
                toast.show()

                Handler(Looper.getMainLooper()).postDelayed({
                    backPressedCount = 0
                }, 2000)
            }
        }
    }

    private fun fetchEmergencyContactAndCall() {
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid
        val dbRef = FirebaseDatabase.getInstance().getReference("Users/$currentUser")

        dbRef.child("emergencyContact").get().addOnSuccessListener { snapshot ->
            val emergencyContact = snapshot.value?.toString()
            if (!emergencyContact.isNullOrEmpty()) {
                val callIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$emergencyContact")
                }
                startActivity(callIntent)
            } else {
                Toast.makeText(this, "No emergency contact set!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch emergency contact.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()
            R.id.nav_upload_documents -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UploadDocumentsFragment()).commit()
            R.id.nav_about -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AboutFragment()).commit()
            R.id.nav_logout -> userLogout()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun userLogout() {
        val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPrefs.edit()
        editor.putBoolean("isLoggedOut", true)
        editor.apply()

        Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show()
        firebaseAuth.signOut()
        val intent = Intent(this@Homepage, LoginAccount::class.java)
        startActivity(intent)
        finish()
    }
}
