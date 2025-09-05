package com.example.sapa

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sapa.databinding.ActivityCoordinatorDashboardBinding

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView

class CoordinatorDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCoordinatorDashboardBinding
    private var loadingDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoordinatorDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentCoordinatorContainer.id, CoordinatorHomeFragment())
                .commit()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.homeFragmentCoordinator -> CoordinatorHomeFragment()
                R.id.profileFragmentCoordinator -> CoordinatorProfileFragment()
                R.id.settingsFragmentCoordinator -> CoordinatorSettingsFragment()
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

