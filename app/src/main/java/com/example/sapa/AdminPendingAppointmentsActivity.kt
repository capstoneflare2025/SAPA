package com.example.sapa

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sapa.databinding.ActivityAdminPendingAppointmentsBinding
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class AdminPendingAppointmentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminPendingAppointmentsBinding
    private lateinit var adapter: AdminPendingAppointmentsAdapter

    // Full list from server
    private val fullAppointmentList = mutableListOf<CoordinatorAppointmentList>()

    // Filtered list shown in RecyclerView
    private val filteredAppointmentList = mutableListOf<CoordinatorAppointmentList>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPendingAppointmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AdminPendingAppointmentsAdapter(this, filteredAppointmentList)
        binding.recyclerAppointmentList.layoutManager = LinearLayoutManager(this)
        binding.recyclerAppointmentList.adapter = adapter

        setupFilterDropdown()
        setupSearchBar()

        fetchPendingAppointments()

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupFilterDropdown() {
        val filterOptions = listOf("All", "Pending", "Approved", "Declined")
        val dropdownAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, filterOptions)
        binding.dropdownFilter.setAdapter(dropdownAdapter)

        // Default selection
        binding.dropdownFilter.setText("All", false)

        binding.dropdownFilter.setOnItemClickListener { _, _, _, _ ->
            applyFilters()
        }
    }

    private fun setupSearchBar() {
        binding.searchId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun applyFilters() {
        val searchText = binding.searchId.text.toString().trim()
        val selectedStatus = binding.dropdownFilter.text.toString()

        filteredAppointmentList.clear()

        val filtered = fullAppointmentList.filter { appointment ->
            val statusMatches = selectedStatus == "All" || appointment.appointmentStatus.equals(selectedStatus, ignoreCase = true)
            val searchMatches = searchText.isEmpty() || appointment.id.contains(searchText, ignoreCase = true)
            statusMatches && searchMatches
        }

        filteredAppointmentList.addAll(filtered)
        adapter.notifyDataSetChanged()

        if (filteredAppointmentList.isEmpty()) {
            Toast.makeText(this, "No appointments found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchPendingAppointments() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/add_appointment/get_appointments.php")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@AdminPendingAppointmentsActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (!response.isSuccessful || body == null) {
                    runOnUiThread {
                        Toast.makeText(this@AdminPendingAppointmentsActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                try {
                    val jsonArray = JSONArray(body)
                    val tempList = mutableListOf<CoordinatorAppointmentList>()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)

                        val id = obj.getString("id")
                        val hospitalName = obj.getString("hospital_name")
                        val status = obj.getString("appointment_status")

                        tempList.add(CoordinatorAppointmentList(id, hospitalName, status))
                    }

                    runOnUiThread {
                        fullAppointmentList.clear()
                        fullAppointmentList.addAll(tempList)

                        applyFilters()  // Apply filters on full data
                    }

                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@AdminPendingAppointmentsActivity, "Parse error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
