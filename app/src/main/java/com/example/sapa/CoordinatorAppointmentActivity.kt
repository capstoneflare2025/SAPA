package com.example.sapa

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sapa.databinding.ActivityCoordinatorAppointmentBinding
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class CoordinatorAppointmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCoordinatorAppointmentBinding
    private lateinit var adapter: CoordinatorAppointmentAdapter
    private val hospitals = mutableListOf<CoordinatorHospital>()  // List to hold hospital data

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCoordinatorAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView() // Set up RecyclerView
        fetchHospitals() // Fetch hospitals data from API

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

    }

    private fun setupRecyclerView() {
        adapter = CoordinatorAppointmentAdapter(this, hospitals) // Pass the list of hospitals to the adapter
        binding.recyclerHospitals.layoutManager = LinearLayoutManager(this)
        binding.recyclerHospitals.adapter = adapter
    }

    private fun fetchHospitals() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/add_hospital/get_hospitals.php")  // Ensure this URL is correct for your XAMPP server
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CoordinatorAppointmentActivity, "Failed to fetch hospitals: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Ensure the response body is not null
                response.body?.string()?.let { json ->

                    println("Response JSON: $json") // Debugging the raw JSON response

                    if (json.trim().startsWith("<!DOCTYPE")) {
                        // Check if the response is HTML (error page)
                        runOnUiThread {
                            Toast.makeText(this@CoordinatorAppointmentActivity, "Invalid response from server (HTML error page)", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }

                    try {
                        val tempList = mutableListOf<CoordinatorHospital>()
                        val arr = JSONArray(json)

                        // Parse the JSON response
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            val id = obj.getString("id")
                            val hospitalName = obj.getString("hospital_name")
                            val hospitalEmail = obj.getString("hospital_email")

                            // Add the hospital data to the list
                            tempList.add(CoordinatorHospital(id, hospitalName,hospitalEmail))
                        }

                        // Run UI thread to update RecyclerView with fetched data
                        runOnUiThread {
                            if (tempList.isEmpty()) {
                                Toast.makeText(this@CoordinatorAppointmentActivity, "No hospitals found", Toast.LENGTH_SHORT).show()
                            }
                            // Update the list in the adapter
                            adapter.updateList(tempList) // Update RecyclerView with fetched hospitals
                        }
                    } catch (e: Exception) {
                        // Handle parsing error
                        runOnUiThread {
                            Toast.makeText(this@CoordinatorAppointmentActivity, "Error parsing data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } ?: run {
                    // Handle the case when response body is null
                    runOnUiThread {
                        Toast.makeText(this@CoordinatorAppointmentActivity, "Empty response body", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
