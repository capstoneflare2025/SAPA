package com.example.sapa

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sapa.databinding.ActivityCoordinatorAppointmentListBinding
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class CoordinatorAppointmentListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCoordinatorAppointmentListBinding
    private lateinit var adapter: CoordinatorAppointmentListAdapter
    private val appointmentsList = mutableListOf<CoordinatorAppointmentList>()
    private val filteredList = mutableListOf<CoordinatorAppointmentList>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoordinatorAppointmentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = CoordinatorAppointmentListAdapter(this, filteredList)
        binding.recyclerAppointmentList.layoutManager = LinearLayoutManager(this)
        binding.recyclerAppointmentList.adapter = adapter

        setupDropdownFilter()
        setupSearchListener()

        fetchAppointments()

        binding.btnAddAppointment.setOnClickListener {
            val intent = Intent(this, CoordinatorAppointmentActivity::class.java)
            startActivity(intent)
        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupDropdownFilter() {
        val statusOptions = listOf("All", "Pending", "Approved", "Declined")
        val adapterStatus = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statusOptions)
        binding.dropdownFilter.setAdapter(adapterStatus)
        binding.dropdownFilter.setText("All", false) // Default to All

        binding.dropdownFilter.setOnItemClickListener { _, _, _, _ ->
            filterAppointments()
        }
    }

    private fun setupSearchListener() {
        binding.searchId.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterAppointments()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // no-op
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // no-op
            }
        })
    }

    private fun filterAppointments() {
        val query = binding.searchId.text.toString().trim()
        val selectedStatus = binding.dropdownFilter.text.toString()

        val results = appointmentsList.filter { appointment ->
            val matchesId = query.isEmpty() || appointment.id.contains(query, ignoreCase = true)
            val matchesStatus = selectedStatus == "All" || appointment.appointmentStatus.equals(selectedStatus, ignoreCase = true)
            matchesId && matchesStatus
        }

        filteredList.clear()
        filteredList.addAll(results)
        adapter.notifyDataSetChanged()

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No appointments found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchAppointments() {
        val sharedPref = getSharedPreferences("SAPA_PREFS", MODE_PRIVATE)
        val coordinatorEmailSession = sharedPref.getString("coordinator_email", null) ?: ""

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/add_appointment/get_appointments.php")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CoordinatorAppointmentListActivity, "Failed to fetch appointments: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string()
                if (!response.isSuccessful || bodyString == null) {
                    runOnUiThread {
                        Toast.makeText(this@CoordinatorAppointmentListActivity, "Failed to fetch appointments", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                try {
                    val jsonArray = JSONArray(bodyString)
                    val tempList = mutableListOf<CoordinatorAppointmentList>()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val coordinatorEmail = obj.optString("coordinator_email", "")

                        if (coordinatorEmail.equals(coordinatorEmailSession, ignoreCase = true)) {
                            val id = obj.getString("id")
                            val hospitalName = obj.getString("hospital_name")
                            val appointmentStatus = obj.getString("appointment_status")

                            tempList.add(CoordinatorAppointmentList(id, hospitalName, appointmentStatus))
                        }
                    }

                    runOnUiThread {
                        appointmentsList.clear()
                        appointmentsList.addAll(tempList)
                        filterAppointments()  // Apply initial filter (which defaults to All + empty search)
                    }

                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@CoordinatorAppointmentListActivity, "Error parsing appointments: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
