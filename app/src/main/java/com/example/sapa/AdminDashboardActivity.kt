package com.example.sapa

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sapa.databinding.ActivityAdminDashboardBinding

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragmentToShow = intent.getStringExtra("show_fragment")

        val fragment = when (fragmentToShow) {
            "AdminHospitalFragment" -> AdminHospitalFragment()
            else -> AdminHomeFragment()
        }

        // Load the fragment based on intent extra or default Home fragment
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentAdminContainer.id, fragment)
            .commit()

        // Bottom navigation listener
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val selectedFragment = when (item.itemId) {
                R.id.homeFragmentAdmin -> AdminHomeFragment()
                R.id.profileFragmentAdmin -> AdminProfileFragment()
                R.id.hospitalFragmentAdmin -> AdminHospitalFragment()
                else -> null
            }

            selectedFragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(binding.fragmentAdminContainer.id, it)
                    .commit()
                true
            } ?: false
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val fragmentToShow = intent.getStringExtra("show_fragment")
        if (fragmentToShow == "AdminHospitalFragment") {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentAdminContainer.id, AdminHospitalFragment())
                .commit()
            binding.bottomNavigation.selectedItemId = R.id.hospitalFragmentAdmin
        }
    }

}

