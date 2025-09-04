    package com.example.sapa

    import android.os.Bundle
    import android.text.Editable
    import android.text.TextWatcher
    import android.widget.ArrayAdapter
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import androidx.recyclerview.widget.LinearLayoutManager
    import com.example.sapa.databinding.ActivityCoordinatorAddStudentBinding
    import okhttp3.Call
    import okhttp3.Callback
    import okhttp3.OkHttpClient
    import okhttp3.Request
    import okhttp3.Response
    import org.json.JSONArray
    import java.io.IOException

    class CoordinatorAddStudentActivity : AppCompatActivity() {

        private lateinit var binding: ActivityCoordinatorAddStudentBinding
        private lateinit var adapter: StudentsAdapter
        private val studentsList = mutableListOf<Students>()   // ✅ master list
        private val schoolsList = mutableListOf<String>()     // ✅ unique school names

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            binding = ActivityCoordinatorAddStudentBinding.inflate(layoutInflater)
            setContentView(binding.root)

            fetchStudents()
            setupRecycler()
            setupSearchBar()  // ✅ search still works

            binding.btnAddStudent.setOnClickListener {
                val dialog = CoordinatorRegisterStudentDialogFragment()
                dialog.setOnStudentAddedListener(object : CoordinatorRegisterStudentDialogFragment.OnStudentAddedListener {
                    override fun onStudentAdded(newStudent: Students) {
                        addStudentToTop(newStudent)
                    }
                })
                dialog.show(supportFragmentManager, "CoordinatorRegisterStudentDialogFragment")
            }

            binding.btnBack.setOnClickListener {
                onBackPressed()
            }
        }

        private fun addStudentToTop(newStudent: Students) {
            studentsList.add(0, newStudent)
            adapter.updateList(studentsList)
            binding.recyclerStudent.scrollToPosition(0)
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
                        Toast.makeText(this@CoordinatorAddStudentActivity, "Failed to fetch", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body?.string()?.let { json ->
                        try {
                            val tempList = mutableListOf<Students>()

                            if (json.trim().startsWith("[")) {
                                val arr = JSONArray(json)
                                for (i in 0 until arr.length()) {
                                    val obj = arr.getJSONObject(i)
                                    val id = obj.getInt("id")
                                    val studentFirstName = obj.getString("student_first_name")
                                    val studentMiddleName = obj.optString("student_middle_name", "")
                                    val studentLastName = obj.getString("student_last_name")
                                    val studentBirthday = obj.optString("student_birthday", "")
                                    val studentGender = obj.optString("student_gender", "")
                                    val studentEmail = obj.optString("student_email", "")
                                    val studentContact = obj.optString("student_contact", "")
                                    val schoolName = obj.getString("school_name")
                                    val coordinatorEmail = obj.optString("coordinator_email", "") // 👈 must come from API

                                    // ✅ Only include students whose school's coordinator matches session
                                    if (coordinatorEmail.equals(coordinatorEmailSession, ignoreCase = true)) {
                                        tempList.add(
                                            Students(
                                                id,
                                                studentFirstName,
                                                studentMiddleName,
                                                studentLastName,
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

                                // ✅ update school dropdown
                                setupDropdownFilter()
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
                                Toast.makeText(this@CoordinatorAddStudentActivity, "Parse error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            })
        }


        private fun setupDropdownFilter() {
            schoolsList.clear()
            schoolsList.add("All") // default
            schoolsList.addAll(studentsList.map { it.schoolName }.distinct())

            val arrayAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                        schoolsList
            )
            binding.dropdownFilter.setAdapter(arrayAdapter)
            binding.dropdownFilter.setText("All", false)

            binding.dropdownFilter.setOnItemClickListener { _, _, position, _ ->
                val selected = schoolsList[position]
                if (selected == "All") {
                    adapter.updateList(studentsList)
                } else {
                    adapter.updateList(studentsList.filter { it.schoolName == selected })
                }
            }
        }

        // ✅ Search students by name
        private fun setupSearchBar() {
            binding.editSearchStudent.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    filterStudents(s.toString())
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }

        private fun filterStudents(query: String) {
            val selectedSchool = binding.dropdownFilter.text.toString()

            val filtered = studentsList.filter { student ->
                val matchesName = student.studentFullName.contains(query, ignoreCase = true)
                val matchesSchool = selectedSchool == "All" || student.schoolName == selectedSchool
                matchesName && matchesSchool
            }

            adapter.updateList(filtered)
        }

        private fun setupRecycler() {
            adapter = StudentsAdapter(studentsList) { action, student ->
                when (action) {
                    "details" -> {
                        // ✅ Pass selected student to dialog
                        val dialog = CoordinatorRegisterStudentDialogFragment.newInstance(student)

                        dialog.setOnStudentUpdatedListener(object : CoordinatorRegisterStudentDialogFragment.OnStudentUpdatedListener {
                            override fun onStudentUpdated(updatedStudent: Students) {
                                updateStudentInList(updatedStudent)
                            }
                        })

                        dialog.show(supportFragmentManager, "StudentDetailsDialog")
                    }

                    "students" -> {
                        Toast.makeText(this, "Open Students for ${student.studentFullName}", Toast.LENGTH_SHORT).show()
                    }
                    "bills" -> {
                        Toast.makeText(this, "Open Bills for ${student.studentFullName}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            binding.recyclerStudent.layoutManager = LinearLayoutManager(this)
            binding.recyclerStudent.adapter = adapter
        }

        private fun updateStudentInList(updatedStudent: Students) {
            val index = studentsList.indexOfFirst { it.id == updatedStudent.id }
            if (index != -1) {
                studentsList[index] = updatedStudent
                adapter.updateList(studentsList)
            }
        }


    }
