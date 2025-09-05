package com.example.sapa

import android.os.Bundle
import android.view.View
import android.widget.Toast
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import androidx.appcompat.app.AppCompatActivity
import com.example.sapa.databinding.ActivityCoordinatorAppointmentDetailsBinding
import kotlin.toString

class CoordinatorAppoinmentDetailsActivity: AppCompatActivity() {

    private lateinit var binding: ActivityCoordinatorAppointmentDetailsBinding
    // 👉 Store appointmentId here


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCoordinatorAppointmentDetailsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val appointmentId = intent.getStringExtra("appointment_id")

        if (appointmentId.isNullOrEmpty()) {
            Toast.makeText(this, "Missing appointment ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        fetchAppointmentDetails(appointmentId)
        // Hide submit button for details-only view
        binding.cancelAppointment.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirm Cancel")
                .setMessage("Are you sure you want to cancel this appointment?")
                .setPositiveButton("Yes") { dialog, _ ->
                    cancelAppointment(appointmentId.toString())
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

    }

    private fun setupUI() {
        binding.backIcon.setOnClickListener {
            finish()
        }

        // Disable input fields (read-only mode)
        makeFieldsReadOnly()
    }

    private fun cancelAppointment(appointmentId: String) {
        val url = "http://192.168.254.193/sapa_api/add_appointment/cancel_appointment.php"

        val client = OkHttpClient()

        val formBody = FormBody.Builder()
            .add("appointment_id", appointmentId)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CoordinatorAppoinmentDetailsActivity, "Cancel failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@CoordinatorAppoinmentDetailsActivity, "Server error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                val responseBody = response.body?.string()
                val json = JSONObject(responseBody ?: "{}")
                val success = json.optBoolean("success", false)

                runOnUiThread {
                    if (success) {
                        Toast.makeText(this@CoordinatorAppoinmentDetailsActivity, "Appointment cancelled", Toast.LENGTH_SHORT).show()

                        // Close this screen and go back to list
                        finish()
                    } else {
                        Toast.makeText(this@CoordinatorAppoinmentDetailsActivity, "Failed to cancel", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }


    private fun fetchAppointmentDetails(id: String) {
        val url = "http://192.168.254.193/sapa_api/add_appointment/get_appointment_details.php?id=$id"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CoordinatorAppoinmentDetailsActivity, "Request failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@CoordinatorAppoinmentDetailsActivity, "Server error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                val responseBody = response.body?.string()
                if (responseBody == null) {
                    runOnUiThread {
                        Toast.makeText(this@CoordinatorAppoinmentDetailsActivity, "Empty response from server", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                try {
                    val json = JSONObject(responseBody)
                    if (json.getBoolean("success")) {
                        val appointment = json.getJSONObject("appointment")

                        runOnUiThread {
                            bindDataToViews(appointment)
                        }
                    } else {
                        val message = json.optString("message", "Unknown error")
                        runOnUiThread {
                            Toast.makeText(this@CoordinatorAppoinmentDetailsActivity, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@CoordinatorAppoinmentDetailsActivity, "Parsing error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun bindDataToViews(data: JSONObject) {
        // School info
        binding.schoolName.setText(data.optString("school_name"))
        binding.schoolEmail.setText(data.optString("school_email"))
        binding.schoolStreet.setText(data.optString("school_street"))
        binding.schoolCity.setText(data.optString("school_city"))
        binding.schoolProvince.setText(data.optString("school_province"))

        // Hospital info
        binding.hospitalName.setText(data.optString("hospital_name"))
        binding.hospitalEmail.setText(data.optString("hospital_email"))
        binding.hospitalAddress.setText(data.optString("hospital_address"))

        // Allocation info
        binding.allocationName.setText(data.optString("allocation_name"))
        binding.allocationSection.setText(data.optString("allocation_section"))
        binding.timeSlot.setText(data.optString("time_slot"))
        binding.billingInfo.setText(data.optString("billing_info"))

        // Coordinator
        binding.coordinatorEmail.setText(data.optString("coordinator_email"))
    }

    private fun makeFieldsReadOnly() {
        val allFields = listOf(
            binding.schoolName,
            binding.schoolEmail,
            binding.schoolStreet,
            binding.schoolCity,
            binding.schoolProvince,

            binding.hospitalName,
            binding.hospitalEmail,
            binding.hospitalAddress,

            binding.allocationName,
            binding.allocationSection,
            binding.timeSlot,
            binding.billingInfo,

            binding.coordinatorEmail
        )

        allFields.forEach { field ->
            field.isEnabled = false
            field.isFocusable = false
            field.isFocusableInTouchMode = false
        }
    }
}