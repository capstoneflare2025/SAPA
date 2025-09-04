package com.example.sapa

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sapa.databinding.ActivityCoordinatorDashboardBinding

class CoordinatorDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCoordinatorDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoordinatorDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load default fragment (Home)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentCoordinatorContainer.id, CoordinatorHomeFragment())
                .commit()
        }

        // Handle bottom navigation with binding
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.homeFragmentCoordinator -> CoordinatorHomeFragment()
                R.id.appointmentFragmentCoordinator -> CoordinatorAppointmentFragment()
                R.id.studentFragmentCoordinator -> CoordinatorStudentFragment()
                R.id.profileFragmentCoordinator -> CoordinatorProfileFragment()
                else -> null
            }

            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(binding.fragmentCoordinatorContainer.id, it)
                    .commit()
                true
            } ?: false
        }
    }
}
