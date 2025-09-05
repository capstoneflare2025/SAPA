package com.example.sapa

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sapa.CoordinatorAppoinmentDetailsActivity
import com.example.sapa.databinding.ActivityAdminAppointmentDetailsBinding
import com.example.sapa.databinding.ActivityCoordinatorAppointmentDetailsBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class AdminAppointmenDetailsActivity: AppCompatActivity() {

    private lateinit var binding: ActivityAdminAppointmentDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminAppointmentDetailsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val appointmentId = intent.getStringExtra("appointment_id")

        if (appointmentId.isNullOrEmpty()) {
            Toast.makeText(this, "Missing appointment ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        fetchAppointmentDetails(appointmentId)


        // Add button click listeners here:
        binding.approveButton.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirm Approval")
                .setMessage("Are you sure you want to approve this appointment?")
                .setPositiveButton("Yes") { dialog, _ ->
                    updateAppointmentStatus(appointmentId, "Approved")
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.declineButton.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirm Decline")
                .setMessage("Are you sure you want to decline this appointment?")
                .setPositiveButton("Yes") { dialog, _ ->
                    updateAppointmentStatus(appointmentId, "Declined")
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

    }

    private fun updateAppointmentStatus(id: String, newStatus: String) {
        val url = "http://192.168.254.193/sapa_api/add_appointment/update_appointment_status.php"

        // Assuming you want to send a POST request with id and newStatus parameters
        val client = OkHttpClient()
        val formBody = okhttp3.FormBody.Builder()
            .add("id", id)  // The column name in your appointments table
            .add("appointment_status", newStatus) // The new status
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@AdminAppointmenDetailsActivity, "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@AdminAppointmenDetailsActivity, "Server error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                val responseBody = response.body?.string()
                if (responseBody != null) {
                    try {
                        val json = JSONObject(responseBody)
                        val success = json.optBoolean("success", false)
                        val message = json.optString("message", "No response message")

                        runOnUiThread {
                            Toast.makeText(this@AdminAppointmenDetailsActivity, message, Toast.LENGTH_SHORT).show()
                            if (success) {
                                // Optionally finish or refresh activity
                                finish()  // Close activity after successful update
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@AdminAppointmenDetailsActivity, "Parsing error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@AdminAppointmenDetailsActivity, "Empty response from server", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }


    private fun setupUI() {
        binding.backIcon.setOnClickListener {
            finish()
        }

        // Disable input fields (read-only mode)
        makeFieldsReadOnly()
    }

    private fun fetchAppointmentDetails(id: String) {
        val url = "http://192.168.254.193/sapa_api/add_appointment/get_appointment_details.php?id=$id"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@AdminAppointmenDetailsActivity, "Request failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@AdminAppointmenDetailsActivity, "Server error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                val responseBody = response.body?.string()
                if (responseBody == null) {
                    runOnUiThread {
                        Toast.makeText(this@AdminAppointmenDetailsActivity, "Empty response from server", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(this@AdminAppointmenDetailsActivity, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@AdminAppointmenDetailsActivity, "Parsing error: ${e.message}", Toast.LENGTH_SHORT).show()
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

        // Coordinator
        binding.coordinatorEmail.setText(data.optString("coordinator_email"))


        // Check status to hide buttons if approved
        val status = data.optString("appointment_status")
        if (status.equals("Approved", ignoreCase = true)) {
            binding.approveButton.visibility = View.GONE
            binding.declineButton.visibility = View.GONE
        } else {
            binding.approveButton.visibility = View.VISIBLE
            binding.declineButton.visibility = View.VISIBLE
        }
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

            binding.coordinatorEmail
        )

        allFields.forEach { field ->
            field.isEnabled = false
            field.isFocusable = false
            field.isFocusableInTouchMode = false
        }
    }
}