package com.example.sapa

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sapa.databinding.ActivityCoordinatorAppointmentBinding
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class CoordinatorAllocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCoordinatorAppointmentBinding
    private lateinit var allocationAdapter: CoordinatorAllocationAdapter
    private val allocationList = mutableListOf<CoordinatorHospitalAllocation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCoordinatorAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the hospital email passed from the previous activity
        val hospitalEmail = intent.getStringExtra("hospital_email") ?: ""

        // Fetch allocation data based on hospital email
        fetchAllocationData(hospitalEmail)

        // Set up RecyclerView
        setupRecyclerView()

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        allocationAdapter = CoordinatorAllocationAdapter(this, allocationList)
        binding.recyclerHospitals.layoutManager = LinearLayoutManager(this)
        binding.recyclerHospitals.adapter = allocationAdapter
    }

    private fun fetchAllocationData(hospitalEmail: String) {
        val client = OkHttpClient()
        val url = "http://192.168.254.193/sapa_api/add_hospital/get_allocation_billing.php?hospital_email=$hospitalEmail"
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CoordinatorAllocationActivity, "Failed to fetch allocation data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonResponse = response.body?.string()

                if (jsonResponse != null) {
                    println("Response JSON: $jsonResponse")

                    // Check if the response is HTML (error page)
                    if (jsonResponse.trim().startsWith("<!DOCTYPE")) {
                        runOnUiThread {
                            Toast.makeText(this@CoordinatorAllocationActivity, "Invalid response from server (HTML error page)", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }

                    try {
                        val arr = JSONArray(jsonResponse)
                        val tempList = mutableListOf<CoordinatorHospitalAllocation>()

                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            val id = obj.getString("id")
                            val allocationName = obj.getString("allocation_name")
                            val timeSlot = obj.getString("time_slot")
                            val billInfo = obj.getString("billing_info")
                            val allocationSection = obj.getString("allocation_section")

                            tempList.add(CoordinatorHospitalAllocation(id, allocationName, timeSlot, billInfo, allocationSection))
                        }

                        runOnUiThread {
                            if (tempList.isEmpty()) {
                                Toast.makeText(this@CoordinatorAllocationActivity, "No allocation data found", Toast.LENGTH_SHORT).show()
                            }
                            allocationList.clear()
                            allocationList.addAll(tempList)
                            allocationAdapter.notifyDataSetChanged()
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@CoordinatorAllocationActivity, "Error parsing data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@CoordinatorAllocationActivity, "Empty response body", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}


