package com.example.sapa

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.sapa.databinding.ActivityAdminHospitalInformationBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class AdminHospitalInformationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminHospitalInformationBinding

    private var isEditing = false // To track if the user is editing the fields

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminHospitalInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the data from the intent
        val hospitalName = intent.getStringExtra("hospitalName")
        val hospitalEmail = intent.getStringExtra("hospitalEmail")
        val contact = intent.getStringExtra("contact")
        val city = intent.getStringExtra("city")
        val type = intent.getStringExtra("type")
        val street = intent.getStringExtra("street")
        val province = intent.getStringExtra("province")
        val allocationName = intent.getStringExtra("allocationName")
        val allocationSection = intent.getStringExtra("allocationSection")
        val timeSlot = intent.getStringExtra("timeSlot")
        val billingInfo = intent.getStringExtra("billingInfo") // Fetch billing info

        // Set the data to views
        binding.hospitalName.setText(hospitalName)
        binding.hospitalEmail.setText(hospitalEmail)
        binding.hospitalContact.setText(contact)
        binding.schoolCity.setText(city)
        binding.hospitalType.setText(type)
        binding.hospitalStreet.setText(street)
        binding.hospitalProvince.setText(province)
        binding.allocationName.setText(allocationName)
        binding.allocationSection.setText(allocationSection)
        binding.timeSlot.setText(timeSlot)
        binding.billingInfo.setText(billingInfo)  // Set billing info

        // Make fields non-editable initially
        setFieldsEditable(false)

        // Handle back button click
        binding.backIcon.setOnClickListener { finish() }

        // Handle edit button click
        binding.editButton.setOnClickListener {
            if (isEditing) {
                showConfirmationDialog()  // Show confirmation dialog when clicking "Save"
            } else {
                setFieldsEditable(true) // Make fields editable
                binding.editButton.text = "Save" // Change button text to Save
                binding.backIcon.visibility = View.GONE // Hide the back icon
                isEditing = true // Change the state to editing
            }
        }
    }

    // Method to toggle editability of fields
    private fun setFieldsEditable(editable: Boolean) {
        binding.hospitalName.isEnabled = editable
        binding.hospitalEmail.isEnabled = editable
        binding.hospitalContact.isEnabled = editable
        binding.schoolCity.isEnabled = editable
        binding.hospitalType.isEnabled = editable
        binding.hospitalStreet.isEnabled = editable
        binding.hospitalProvince.isEnabled = editable
        binding.allocationName.isEnabled = editable
        binding.allocationSection.isEnabled = editable
        binding.timeSlot.isEnabled = editable
        binding.billingInfo.isEnabled = editable
    }

    // Method to display a confirmation dialog before updating the hospital info
    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Update")
        builder.setMessage("Are you sure you want to save the changes?")
        builder.setPositiveButton("Yes") { _, _ ->
            updateHospitalInfo() // Send data to backend and update
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss() // Close the dialog without making any changes
        }
        builder.show()
    }

    // Method to update hospital info (send to backend)
    private fun updateHospitalInfo() {
        val url = "http://192.168.254.193/sapa_api/add_hospital/update_hospital_info.php"

        val client = OkHttpClient()
        val json = JSONObject()

        // Prepare JSON data
        json.put("hospitalName", binding.hospitalName.text.toString())
        json.put("hospitalEmail", binding.hospitalEmail.text.toString())
        json.put("contact", binding.hospitalContact.text.toString())
        json.put("city", binding.schoolCity.text.toString())
        json.put("type", binding.hospitalType.text.toString())
        json.put("street", binding.hospitalStreet.text.toString())
        json.put("province", binding.hospitalProvince.text.toString())
        json.put("allocationName", binding.allocationName.text.toString())
        json.put("allocationSection", binding.allocationSection.text.toString())
        json.put("timeSlot", binding.timeSlot.text.toString())
        json.put("billingInfo", binding.billingInfo.text.toString())

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(), json.toString()
        )

        // Make the API request to update the hospital info
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@AdminHospitalInformationActivity, "Failed to update hospital info", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@AdminHospitalInformationActivity, "Hospital info updated successfully", Toast.LENGTH_SHORT).show()
                        finish() // Optionally close the activity after success
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@AdminHospitalInformationActivity, "Failed to update hospital info", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
