package com.example.sapa

import android.content.Intent
import androidx.appcompat.app.AlertDialog
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


        binding.backIcon.setOnClickListener {
            navigateBackToFragment()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 2001 && resultCode == RESULT_OK && data != null) {
            // Retrieve hospital data returned from BillingAllocationActivity
            val hospitalName = data.getStringExtra("hospital_name")
            val hospitalEmail = data.getStringExtra("hospital_email")
            val hospitalContact = data.getStringExtra("hospital_contact")
            val hospitalType = data.getStringExtra("hospital_type")
            val hospitalStreet = data.getStringExtra("hospital_street")
            val hospitalCity = data.getStringExtra("hospital_city")
            val hospitalProvince = data.getStringExtra("hospital_province")

            // Set the EditText fields with this data to keep it visible
            binding.hospitalName.setText(hospitalName)
            binding.hospitalEmail.setText(hospitalEmail)
            binding.hospitalContact.setText(hospitalContact)
            binding.hospitalTypeDropdown.setText(hospitalType)
            binding.hospitalStreet.setText(hospitalStreet)
            binding.hospitalCity.setText(hospitalCity)
            binding.hospitalProvince.setText(hospitalProvince)
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
        startActivityForResult(intent, 2001)
    }

    private fun navigateBackToFragment() {
        // Check if any field has data entered
        val isAnyFieldFilled = listOf(
            binding.hospitalName.text.toString(),
            binding.hospitalEmail.text.toString(),
            binding.hospitalContact.text.toString(),
            binding.hospitalTypeDropdown.text.toString(),
            binding.hospitalStreet.text.toString(),
            binding.hospitalCity.text.toString(),
            binding.hospitalProvince.text.toString()
        ).any { it.isNotBlank() }

        if (isAnyFieldFilled) {
            // Show confirmation dialog
            AlertDialog.Builder(this)
                .setTitle("Discard Changes?")
                .setMessage("You have unsaved changes. Are you sure you want to go back? All entered data will be lost.")
                .setPositiveButton("Yes, discard") { dialog, _ ->
                    dialog.dismiss()
                    finish()  // Close activity and discard changes
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss() // Just dismiss dialog and stay
                }
                .show()
        } else {
            // No data entered, just finish
            finish()
        }
    }

}
