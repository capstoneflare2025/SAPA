package com.example.sapa

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sapa.databinding.ActivityCoordinatorSelectSchoolBinding
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class CoordinatorSelectSchoolActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCoordinatorSelectSchoolBinding
    private lateinit var adapter: CoordinatorSelectSchoolAdapter
    private val schoolList = mutableListOf<SelectSchool>()

    // Variables to hold hospital & allocation info passed from previous activity
    private var hospitalEmail: String? = null
    private var hospitalName: String? = null
    private var hospitalAddress: String? = null

    private var allocationName: String? = null
    private var allocationSection: String? = null
    private var allocationTimeSlot: String? = null
    private var allocationBillingInfo: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoordinatorSelectSchoolBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Receive hospital & allocation data from intent extras
        hospitalEmail = intent.getStringExtra("hospital_email")
        hospitalName = intent.getStringExtra("hospital_name")
        hospitalAddress = intent.getStringExtra("hospital_address")

        allocationName = intent.getStringExtra("allocation_name")
        allocationSection = intent.getStringExtra("allocation_section")
        allocationTimeSlot = intent.getStringExtra("time_slot")
        allocationBillingInfo = intent.getStringExtra("billing_info")

        setupRecycler()
        setupSearchBar()
        fetchSchools()

        binding.btnBack.setOnClickListener { onBackPressed() }
    }

    private fun fetchSchools() {
        val sharedPref = getSharedPreferences("SAPA_PREFS", MODE_PRIVATE)
        val coordinatorEmailSession = sharedPref.getString("coordinator_email", null) ?: ""

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/add_school/get_schools.php")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CoordinatorSelectSchoolActivity, "Failed to fetch schools", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { json ->
                    try {
                        val tempList = mutableListOf<SelectSchool>()
                        val jsonArray = if (json.trim().startsWith("[")) JSONArray(json)
                        else JSONObject(json).getJSONArray("schools")

                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)
                            val coordinatorEmail = obj.optString("coordinator_email", "")
                            val schoolStatus = obj.optString("school_status", "Pending")

                            if (coordinatorEmail.equals(coordinatorEmailSession, ignoreCase = true)
                                && schoolStatus.equals("approved", ignoreCase = true)
                            ) {
                                tempList.add(
                                    SelectSchool(
                                        id = obj.getInt("id"),
                                        schoolName = obj.getString("school_name"),
                                        schoolEmail = obj.optString("school_email", ""),
                                        schoolStreet = obj.optString("school_street", ""),
                                        schoolCity = obj.optString("school_city", ""),
                                        schoolProvince = obj.optString("school_province", ""),
                                        schoolAddress = obj.optString("school_address", "")
                                    )
                                )
                            }
                        }

                        runOnUiThread {
                            schoolList.clear()
                            schoolList.addAll(tempList)
                            adapter.updateList(schoolList)
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@CoordinatorSelectSchoolActivity, "Parse error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    private fun setupSearchBar() {
        binding.searchId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterSchools(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterSchools(query: String) {
        val filtered = if (query.isEmpty()) {
            schoolList
        } else {
            schoolList.filter {
                it.id.toString().contains(query, ignoreCase = true) ||
                        it.schoolName.contains(query, ignoreCase = true) ||
                        it.schoolEmail.contains(query, ignoreCase = true) ||
                        listOf(it.schoolProvince, it.schoolCity, it.schoolStreet)
                            .joinToString(", ")
                            .contains(query, ignoreCase = true)
            }
        }
        adapter.updateList(filtered)

        if (filtered.isEmpty()) {
            Toast.makeText(this, "No matching schools found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecycler() {
        adapter = CoordinatorSelectSchoolAdapter(
            emptyList(),
            hospitalEmail,
            hospitalName,
            hospitalAddress,
            allocationName,
            allocationSection,
            allocationTimeSlot,
            allocationBillingInfo
        ) { _, _ -> }
        binding.recyclerSelectSchools.layoutManager = LinearLayoutManager(this)
        binding.recyclerSelectSchools.adapter = adapter
    }
}
