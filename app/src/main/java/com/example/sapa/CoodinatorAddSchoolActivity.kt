package com.example.sapa

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sapa.databinding.ActivityCoordinatorAddSchoolBinding
import com.example.sapa.databinding.DialogCoordinatorStudentsBinding
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class CoodinatorAddSchoolActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCoordinatorAddSchoolBinding
    private lateinit var adapter: SchoolAdapter
    private val schoolList = mutableListOf<School>()   // ✅ original list
    private val handler = Handler(Looper.getMainLooper())

    // 🔄 Polling every 5 seconds
    private val refreshRunnable = object : Runnable {
        override fun run() {
            fetchSchools()
            handler.postDelayed(this, 5000) // refresh every 5 sec
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCoordinatorAddSchoolBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecycler()
        setupDropdownFilter()
        setupSearchBar()
        fetchSchools()

        binding.btnAddSchool.setOnClickListener {
            val dialog = CoordinatorRegisterSchoolDialogFragment()
            dialog.setOnSchoolAddedListener(object : CoordinatorRegisterSchoolDialogFragment.OnSchoolAddedListener {
                override fun onSchoolAdded(newSchool: School) {
                    addSchoolToTop(newSchool)
                }
            })
            dialog.show(supportFragmentManager, "CoordinatorAddSchoolDialogFragment")
        }


        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }

    private fun fetchSchools() {
        val sharedPref = getSharedPreferences("SAPA_PREFS", MODE_PRIVATE)
        val coordinatorEmailSession = sharedPref.getString("coordinator_email", null)

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/add_school/get_schools.php")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CoodinatorAddSchoolActivity, "Failed to fetch", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { json ->
                    try {
                        val tempList = mutableListOf<School>()

                        if (json.trim().startsWith("[")) {
                            val arr = JSONArray(json)
                            for (i in 0 until arr.length()) {
                                val obj = arr.getJSONObject(i)
                                val id = obj.getInt("id")
                                val schoolName = obj.getString("school_name")
                                val schoolStatus = obj.optString("school_status", "pending")
                                val schoolEmail = obj.optString("school_email", "")
                                val schoolContact = obj.optString("school_contact", "")
                                val schoolStreet = obj.optString("school_street", "")
                                val schoolCity = obj.optString("school_city", "")
                                val schoolProvince = obj.optString("school_province", "")
                                val schoolAddress = obj.optString("school_address", "")
                                val coordinatorEmail = obj.optString("coordinator_email", "") // 👈 add this

                                // ✅ Only include schools for this coordinator
                                if (coordinatorEmail.equals(coordinatorEmailSession, ignoreCase = true)) {
                                    tempList.add(
                                        School(
                                            id,
                                            schoolName,
                                            schoolStatus,
                                            schoolEmail,
                                            schoolContact,
                                            schoolStreet,
                                            schoolCity,
                                            schoolProvince,
                                            schoolAddress
                                        )
                                    )
                                }
                            }
                        } else {
                            val root = JSONObject(json)
                            if (root.has("schools")) {
                                val arr = root.getJSONArray("schools")
                                for (i in 0 until arr.length()) {
                                    val obj = arr.getJSONObject(i)
                                    val id = obj.getInt("id")
                                    val schoolName = obj.getString("school_name")
                                    val schoolStatus = obj.optString("school_status", "pending")
                                    val schoolEmail = obj.optString("school_email", "")
                                    val schoolContact = obj.optString("school_contact", "")
                                    val schoolStreet = obj.optString("school_street", "")
                                    val schoolCity = obj.optString("school_city", "")
                                    val schoolProvince = obj.optString("school_province", "")
                                    val schoolAddress = obj.optString("school_address", "")
                                    val coordinatorEmail = obj.optString("coordinator_email", "") // 👈 add this

                                    // ✅ Only include schools for this coordinator
                                    if (coordinatorEmail.equals(coordinatorEmailSession, ignoreCase = true)) {
                                        tempList.add(
                                            School(
                                                id,
                                                schoolName,
                                                schoolStatus,
                                                schoolEmail,
                                                schoolContact,
                                                schoolStreet,
                                                schoolCity,
                                                schoolProvince,
                                                schoolAddress
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        runOnUiThread {
                            schoolList.clear()
                            schoolList.addAll(tempList)
                            adapter.updateList(schoolList)
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@CoodinatorAddSchoolActivity, "Parse error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }


    // ✅ Instantly add new school to top of RecyclerView
    private fun addSchoolToTop(newSchool: School) {
        schoolList.add(0, newSchool)  // insert at top
        adapter.updateList(schoolList)
        binding.recyclerSchools.scrollToPosition(0)
    }

    private fun setupDropdownFilter() {
        val options = listOf("All", "Pending", "Approved", "Declined")
        val arrayAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            options
        )
        binding.dropdownFilter.setAdapter(arrayAdapter)
        binding.dropdownFilter.setText("All", false)

        binding.dropdownFilter.setOnItemClickListener { _, _, position, _ ->
            val selected = options[position]
            when (selected) {
                "All" -> adapter.updateList(schoolList)
                "Pending" -> adapter.updateList(schoolList.filter { it.schoolStatus.equals("pending", true) })
                "Approved" -> adapter.updateList(schoolList.filter { it.schoolStatus.equals("approved", true) })
                "Declined" -> adapter.updateList(schoolList.filter { it.schoolStatus.equals("declined", true) })
            }
        }
    }

    // ✅ Search function
    private fun setupSearchBar() {
        binding.editSearchSchool.addTextChangedListener(object : TextWatcher {
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
            schoolList.filter { it.schoolName.contains(query, ignoreCase = true) }
        }
        adapter.updateList(filtered)
    }

    private fun setupRecycler() {
        adapter = SchoolAdapter(schoolList) { action, school ->
            when (action) {
                "details" -> {
                    val dialog = CoordinatorRegisterSchoolDialogFragment.newInstance(school)
                    dialog.setOnSchoolUpdatedListener(object : CoordinatorRegisterSchoolDialogFragment.OnSchoolUpdatedListener {
                        override fun onSchoolUpdated(updatedSchool: School) {
                            updateSchoolInList(updatedSchool)
                        }
                    })
                    dialog.show(supportFragmentManager, "SchoolDetailsDialog")
                }

                // ✅ Students now displayed in TableLayout dialog instead of RecyclerView
                // ✅ Students now displayed in TableLayout dialog instead of RecyclerView
                "students" -> {
                    // Pass school.schoolName, not school.id
                    fetchStudentsForSchool(school.schoolName) { students ->
                        showStudentsTableDialog(school.schoolName, students)
                    }
                }


                "bills" -> {
                    Toast.makeText(this, "Open Bills for ${school.schoolName}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.recyclerSchools.layoutManager = LinearLayoutManager(this)
        binding.recyclerSchools.adapter = adapter
    }

    private fun fetchStudentsForSchool(schoolName: String, callback: (List<Students>) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/add_student/get_all_students.php")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CoodinatorAddSchoolActivity, "Failed to fetch students", Toast.LENGTH_SHORT).show()
                    callback(emptyList())
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { json ->
                    try {
                        val tempList = mutableListOf<Students>()
                        val arr = JSONArray(json)

                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            val studentSchool = obj.getString("school_name")

                            // Only include students for this school
                            if (studentSchool.equals(schoolName, ignoreCase = true)) {
                                tempList.add(
                                    Students(
                                        id = obj.getInt("id"),
                                        studentFirstName = obj.getString("student_first_name"),
                                        studentMiddleName = obj.getString("student_middle_name"),
                                        studentLastName = obj.getString("student_last_name"),
                                        schoolName = studentSchool
                                    )
                                )
                            }
                        }

                        runOnUiThread {
                            callback(tempList)
                        }

                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@CoodinatorAddSchoolActivity, "Parse error: ${e.message}", Toast.LENGTH_SHORT).show()
                            callback(emptyList())
                        }
                    }
                }
            }
        })
    }


    private fun updateSchoolInList(updated: School) {
        val index = schoolList.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            schoolList[index] = updated
            adapter.updateList(schoolList)
            binding.recyclerSchools.scrollToPosition(index)
        }
    }

    private fun showStudentsTableDialog(schoolName: String, students: List<Students>) {
        val dialogBinding = DialogCoordinatorStudentsBinding.inflate(layoutInflater)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        // Set dialog title
        dialogBinding.txtSchoolTitle.text = "Students of $schoolName"

        // Clear table
        dialogBinding.tableStudents.removeAllViews()

        // Add table header
        val headerRow = TableRow(this)
        val headerNumber = TextView(this).apply { text = "#" ; setPadding(16,16,16,16) }
        val headerName = TextView(this).apply { text = "Student Name"; setPadding(16,16,16,16) }
        val headerSchool = TextView(this).apply { text = "School"; setPadding(16,16,16,16) }
        headerRow.addView(headerNumber)
        headerRow.addView(headerName)
        headerRow.addView(headerSchool)
        dialogBinding.tableStudents.addView(headerRow)

        // Populate table
        students.forEachIndexed { index, student ->
            val row = TableRow(this)
            val txtNumber = TextView(this).apply { text = (index + 1).toString(); setPadding(16,16,16,16) }
            val txtName = TextView(this).apply {
                text = "${student.studentFirstName} ${student.studentMiddleName} ${student.studentLastName}"
                setPadding(16,16,16,16)
            }
            val txtSchool = TextView(this).apply {
                text = student.schoolName
                setPadding(16,16,16,16)
            }

            row.addView(txtNumber)
            row.addView(txtName)
            row.addView(txtSchool)
            dialogBinding.tableStudents.addView(row)
        }

        dialogBinding.iconBack.setOnClickListener { dialog.dismiss() }

        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels*0.95).toInt(),
            (resources.displayMetrics.heightPixels*0.85).toInt()
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }


}
