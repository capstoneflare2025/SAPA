package com.example.sapa

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sapa.databinding.ActivityCoordinatorAllocationBinding
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class CoordinatorAllocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCoordinatorAllocationBinding
    private lateinit var allocationAdapter: CoordinatorAllocationAdapter

    private val allocationList = mutableListOf<CoordinatorHospitalAllocation>()      // Full list from API
    private val filteredList = mutableListOf<CoordinatorHospitalAllocation>()        // Filtered list for adapter

    private var hospitalEmail: String = ""
    private var hospitalName: String = ""
    private var hospitalAddress: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCoordinatorAllocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get hospital data from intent extras
        hospitalEmail = intent.getStringExtra("hospital_email") ?: ""
        hospitalName = intent.getStringExtra("hospital_name") ?: ""
        hospitalAddress = intent.getStringExtra("hospital_address") ?: ""

        setupRecyclerView()
        setupSearchFilter()
        fetchAllocationData(hospitalEmail)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        allocationAdapter = CoordinatorAllocationAdapter(this, filteredList, hospitalEmail, hospitalName, hospitalAddress)
        binding.recyclerHospitals.layoutManager = LinearLayoutManager(this)
        binding.recyclerHospitals.adapter = allocationAdapter
    }

    private fun setupSearchFilter() {
        binding.searchId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterAllocationsById()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterAllocationsById() {
        val query = binding.searchId.text.toString().trim()

        val result = if (query.isEmpty()) {
            allocationList
        } else {
            allocationList.filter { it.id.contains(query, ignoreCase = true) }
        }

        filteredList.clear()
        filteredList.addAll(result)
        allocationAdapter.notifyDataSetChanged()

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No matching allocations found", Toast.LENGTH_SHORT).show()
        }
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
                            val allocationSection = obj.getString("allocation_section")
                            val timeSlot = obj.getString("time_slot")
                            val billingInfo = obj.getString("billing_info")

                            tempList.add(
                                CoordinatorHospitalAllocation(
                                    id,
                                    allocationName,
                                    timeSlot,
                                    billingInfo,
                                    allocationSection
                                )
                            )

                        }

                        runOnUiThread {
                            allocationList.clear()
                            allocationList.addAll(tempList)

                            filteredList.clear()
                            filteredList.addAll(tempList)

                            allocationAdapter.notifyDataSetChanged()

                            if (tempList.isEmpty()) {
                                Toast.makeText(this@CoordinatorAllocationActivity, "No allocation data found", Toast.LENGTH_SHORT).show()
                            }
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
