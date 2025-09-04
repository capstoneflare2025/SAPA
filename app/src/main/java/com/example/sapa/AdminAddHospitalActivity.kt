package com.example.sapa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sapa.databinding.ActivityAdminAddHospitalBinding

class AdminAddHospitalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminAddHospitalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminAddHospitalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the dropdown for hospital type
        val hospitalTypes = listOf("Private", "Public")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, hospitalTypes)
        binding.hospitalTypeDropdown.setAdapter(adapter)

        // Retrieve the data passed via Intent when coming back to this activity
        val hospitalName = intent.getStringExtra("hospital_name")
        val hospitalEmail = intent.getStringExtra("hospital_email")
        val hospitalContact = intent.getStringExtra("hospital_contact")
        val hospitalType = intent.getStringExtra("hospital_type")
        val hospitalStreet = intent.getStringExtra("hospital_street")
        val hospitalCity = intent.getStringExtra("hospital_city")
        val hospitalProvince = intent.getStringExtra("hospital_province")

        // Pre-fill the fields with the data
        binding.hospitalName.setText(hospitalName)
        binding.hospitalEmail.setText(hospitalEmail)
        binding.hospitalContact.setText(hospitalContact)
        binding.hospitalTypeDropdown.setText(hospitalType)
        binding.hospitalStreet.setText(hospitalStreet)
        binding.hospitalCity.setText(hospitalCity)
        binding.hospitalProvince.setText(hospitalProvince)

        // Handle back and next button clicks
        binding.buttonNext.setOnClickListener {
            // Collect form data
            val hospitalName = binding.hospitalName.text.toString()
            val hospitalEmail = binding.hospitalEmail.text.toString()
            val hospitalContact = binding.hospitalContact.text.toString()
            val hospitalType = binding.hospitalTypeDropdown.text.toString() // Get selected hospital type
            val hospitalStreet = binding.hospitalStreet.text.toString()
            val hospitalCity = binding.hospitalCity.text.toString()
            val hospitalProvince = binding.hospitalProvince.text.toString()

            // Check if required fields are filled
            if (hospitalName.isNotEmpty() && hospitalEmail.isNotEmpty() && hospitalContact.isNotEmpty() && hospitalType.isNotEmpty()) {
                sendHospitalDataToNextActivity(
                    hospitalName,
                    hospitalEmail,
                    hospitalContact,
                    hospitalType,
                    hospitalStreet,
                    hospitalCity,
                    hospitalProvince
                )
            } else {
                Toast.makeText(this, "Please fill out all required fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle back button click to navigate to previous screen or fragment
        binding.buttonBack.setOnClickListener {
            navigateBackToFragment()
        }

        binding.backIcon.setOnClickListener {
            navigateBackToFragment()
        }
    }

    // Function to send hospital data to the next activity
    private fun sendHospitalDataToNextActivity(
        hospitalName: String,
        hospitalEmail: String,
        hospitalContact: String,
        hospitalType: String,
        hospitalStreet: String,
        hospitalCity: String,
        hospitalProvince: String
    ) {
        val intent = Intent(this, AdminAddHospitalBillingAllocationActivity::class.java).apply {
            putExtra("hospital_name", hospitalName)
            putExtra("hospital_email", hospitalEmail)
            putExtra("hospital_contact", hospitalContact)
            putExtra("hospital_type", hospitalType)
            putExtra("hospital_street", hospitalStreet)
            putExtra("hospital_city", hospitalCity)
            putExtra("hospital_province", hospitalProvince)
        }
        startActivity(intent)
    }

    private fun navigateBackToFragment() {
        // Create the fragment instance
        val fragment = AdminHospitalFragment()
        val transaction = supportFragmentManager.beginTransaction()

        val fragmentTag = AdminHospitalFragment::class.java.simpleName

        // Check if the fragment is already added to the back stack
        val existingFragment = supportFragmentManager.findFragmentByTag(fragmentTag)

        if (existingFragment == null) {
            // If the fragment is not already in the back stack, add it
            transaction.replace(R.id.fragment_container_hospital, fragment, fragmentTag)
            transaction.addToBackStack(fragmentTag)  // Use the fragment tag to avoid duplication
            transaction.commit()  // Use commit() instead of commitNow()
        } else {
            Log.d("AdminAddHospital", "Fragment already in the back stack, not adding again.")
        }
    }

}
