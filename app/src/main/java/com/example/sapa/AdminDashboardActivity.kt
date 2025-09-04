package com.example.sapa

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sapa.databinding.ActivityAdminDashboardBinding

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load default fragment (Home)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentAdminContainer.id, AdminHomeFragment())
                .commit()
        }

        // Handle bottom navigation with binding
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.homeFragmentAdmin -> AdminHomeFragment()
                R.id.profileFragmentAdmin-> AdminProfileFragment()
                R.id.hospitalFragmentAdmin-> AdminHospitalFragment()
                else -> null
            }

            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(binding.fragmentAdminContainer.id, it)
                    .commit()
                true
            } ?: false
        }
    }
}
