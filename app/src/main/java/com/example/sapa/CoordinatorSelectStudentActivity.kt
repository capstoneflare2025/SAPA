package com.example.sapa

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sapa.databinding.ActivityCoordinatorSelectStudentBinding
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class CoordinatorSelectStudentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCoordinatorSelectStudentBinding
    private lateinit var adapter: CoordinatorSelectStudentAdapter
    private lateinit var loadingLayout: LinearLayout


    private val studentsList = mutableListOf<SelectStudents>()

    // School info
    private var selectedSchoolId: Int = -1
    private var selectedSchoolName: String? = null
    private var selectedSchoolEmail: String? = null
    private var selectedSchoolStreet: String? = null
    private var selectedSchoolCity: String? = null
    private var selectedSchoolProvince: String? = null
    private var selectedSchoolAddress: String? = null

    // Hospital info
    private var hospitalEmail: String? = null
    private var hospitalName: String? = null
    private var hospitalAddress: String? = null

    // Allocation info
    private var allocationName: String? = null
    private var allocationSection: String? = null
    private var allocationTimeSlot: String? = null
    private var allocationBillingInfo: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoordinatorSelectStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Receive all passed data from Intent
        selectedSchoolId = intent.getIntExtra("school_id", -1)
        selectedSchoolName = intent.getStringExtra("school_name")
        selectedSchoolEmail = intent.getStringExtra("school_email")
        selectedSchoolStreet = intent.getStringExtra("school_street")
        selectedSchoolCity = intent.getStringExtra("school_city")
        selectedSchoolProvince = intent.getStringExtra("school_province")
        selectedSchoolAddress = intent.getStringExtra("school_address")

        hospitalEmail = intent.getStringExtra("hospital_email")
        hospitalName = intent.getStringExtra("hospital_name")
        hospitalAddress = intent.getStringExtra("hospital_address")

        allocationName = intent.getStringExtra("allocation_name")
        allocationSection = intent.getStringExtra("allocation_section")
        allocationTimeSlot = intent.getStringExtra("time_slot")
        allocationBillingInfo = intent.getStringExtra("billing_info")

        setupRecycler()
        setupSearchBar()
        fetchStudents()

        binding.btnBack.setOnClickListener { onBackPressed() }

        binding.btnSubmit.setOnClickListener {
            val selectedStudents = adapter.getSelectedStudents()
            if (selectedStudents.isEmpty()) {
                Toast.makeText(this, "No students selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendSelectedStudentsToServer(selectedStudents)
        }

        binding.checkboxSelectAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                adapter.selectAll()
            } else {
                adapter.clearSelection()
            }
        }


    }

    private fun fetchStudents() {
        val sharedPref = getSharedPreferences("SAPA_PREFS", MODE_PRIVATE)
        val coordinatorEmailSession = sharedPref.getString("coordinator_email", null)

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/add_school/get_students.php")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CoordinatorSelectStudentActivity, "Failed to fetch students", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { json ->
                    try {
                        val tempList = mutableListOf<SelectStudents>()

                        if (json.trim().startsWith("[")) {
                            val arr = JSONArray(json)
                            for (i in 0 until arr.length()) {
                                val obj = arr.getJSONObject(i)
                                val id = obj.getInt("id")
                                val studentFirstName = obj.getString("student_first_name")
                                val studentMiddleName = obj.optString("student_middle_name", "")
                                val studentLastName = obj.getString("student_last_name")
                                val studentEmail = obj.optString("student_email", "")
                                val schoolName = obj.getString("school_name")
                                val coordinatorEmail = obj.optString("coordinator_email", "")

                                if (
                                    coordinatorEmail.equals(coordinatorEmailSession, ignoreCase = true) &&
                                    schoolName.equals(selectedSchoolName, ignoreCase = true)
                                ) {
                                    tempList.add(
                                        SelectStudents(
                                            id,
                                            studentFirstName,
                                            studentMiddleName,
                                            studentLastName,
                                            studentEmail,
                                            schoolName
                                        )
                                    )
                                }
                            }
                        }

                        runOnUiThread {
                            studentsList.clear()
                            studentsList.addAll(tempList)
                            adapter.updateList(studentsList)
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@CoordinatorSelectStudentActivity, "Parse error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    private fun setupRecycler() {
        adapter = CoordinatorSelectStudentAdapter(studentsList) { _, _ -> }
        binding.recyclerStudent.layoutManager = LinearLayoutManager(this)
        binding.recyclerStudent.adapter = adapter
    }

    private fun setupSearchBar() {
        binding.searchId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                val filtered = studentsList.filter {
                    it.id.toString().contains(query)
                }
                adapter.updateList(filtered)
            }
        })
    }


    private fun sendSelectedStudentsToServer(selectedStudents: List<SelectStudents>) {
        val client = OkHttpClient()
        val sharedPref = getSharedPreferences("SAPA_PREFS", MODE_PRIVATE)
        val coordinatorEmailSession = sharedPref.getString("coordinator_email", null)

        val formBuilder = FormBody.Builder()
        formBuilder.add("school_name", selectedSchoolName ?: "")
        formBuilder.add("school_email", selectedSchoolEmail ?: "")
        formBuilder.add("school_street", selectedSchoolStreet ?: "")
        formBuilder.add("school_city", selectedSchoolCity ?: "")
        formBuilder.add("school_province", selectedSchoolProvince ?: "")
        formBuilder.add("school_address", selectedSchoolAddress ?: "")

        // Add hospital details
        formBuilder.add("hospital_email", hospitalEmail ?: "")
        formBuilder.add("hospital_name", hospitalName ?: "")
        formBuilder.add("hospital_address", hospitalAddress ?: "")

        // Add allocation details
        formBuilder.add("allocation_name", allocationName ?: "")
        formBuilder.add("allocation_section", allocationSection ?: "")
        formBuilder.add("time_slot", allocationTimeSlot ?: "")
        formBuilder.add("billing_info", allocationBillingInfo ?: "")
        formBuilder.add("appointment_status", "Pending") // ✅ Add it here

        formBuilder.add("coordinator_email", coordinatorEmailSession ?: "")


        val studentsJsonArray = JSONArray()

        for (student in selectedStudents) {
            val obj = JSONObject()
            obj.put("student_full_name", student.studentFullName)
            obj.put("student_email", student.studentEmail)
            studentsJsonArray.put(obj)
        }

        Log.d("DEBUG_APPOINTMENT", "JSON to send: ${studentsJsonArray.toString()}")
        formBuilder.add("students", studentsJsonArray.toString())

        val formBody = formBuilder.build()

        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/add_appointment/add_appointment.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DEBUG_APPOINTMENT", "Network failure: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@CoordinatorSelectStudentActivity, "Failed to save appointments", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("DEBUG_APPOINTMENT", "Server response: $responseBody")

                runOnUiThread {
                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val json = JSONObject(responseBody)
                            val success = json.optBoolean("success", false)
                            val message = json.optString("message", "No message")

                            Log.d("DEBUG_APPOINTMENT", "Parsed response - success: $success, message: $message")

                            if (success) {
                                Toast.makeText(this@CoordinatorSelectStudentActivity, "Appointments saved successfully", Toast.LENGTH_SHORT).show()
                                adapter.clearSelection()

                                val intent = Intent(this@CoordinatorSelectStudentActivity, CoordinatorDashboardActivity::class.java)
                                startActivity(intent)

                            }
                            else {
                                Toast.makeText(this@CoordinatorSelectStudentActivity, "Error: $message", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@CoordinatorSelectStudentActivity, "Invalid response format", Toast.LENGTH_LONG).show()
                            Log.e("DEBUG_APPOINTMENT", "JSON parse error: ${e.message}")
                        }
                    } else {
                        Toast.makeText(this@CoordinatorSelectStudentActivity, "HTTP Error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        })
    }
}
