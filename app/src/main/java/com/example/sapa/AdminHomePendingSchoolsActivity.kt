package com.example.sapa

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sapa.databinding.ActivityAdminPendingSchoolsBinding
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class AdminHomePendingSchoolsActivity : AppCompatActivity(),
    AdminPendingSchoolDetailsDialogFragment.OnSchoolUpdatedListener {

    private lateinit var binding: ActivityAdminPendingSchoolsBinding
    private lateinit var adapter: PendingSchoolAdapter
    private var allSchools = mutableListOf<PendingSchool>()
    private var selectedStatus: String = "All" // keep track of current filter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPendingSchoolsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Setup RecyclerView
        adapter = PendingSchoolAdapter(mutableListOf())
        binding.recyclerPendingSchools.layoutManager = LinearLayoutManager(this)
        binding.recyclerPendingSchools.adapter = adapter

        adapter.onViewDetails = { school ->
            showSchoolDetailsDialog(school)
        }

        // ✅ Setup Dropdown filter
        val statuses = listOf("All", "Pending", "Approved", "Declined")
        val dropdownAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statuses)
        binding.filterSpinner.setAdapter(dropdownAdapter)

        binding.filterSpinner.setOnItemClickListener { _, _, position, _ ->
            selectedStatus = statuses[position]
            filterSchools(selectedStatus)
        }

        // ✅ Fetch all schools
        fetchAllSchools()

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun fetchAllSchools() {
        thread {
            try {
                val url = URL("http://192.168.254.193/sapa_api/add_school/get_all_schools.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                val data = conn.inputStream.bufferedReader().readText()
                val jsonArray = JSONArray(data)

                val schools = mutableListOf<PendingSchool>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    schools.add(
                        PendingSchool(
                            obj.getInt("id"),
                            obj.getString("school_name"),
                            obj.getString("school_email"),
                            obj.getString("school_contact"),
                            obj.getString("school_street"),
                            obj.getString("school_city"),
                            obj.getString("school_province"),
                            obj.getString("school_address"),
                            obj.getString("coordinator_name"),
                            obj.getString("coordinator_email"),
                            obj.getString("school_status")
                        )
                    )
                }

                runOnUiThread {
                    allSchools = schools
                    adapter.updateData(allSchools) // show all by default
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Error fetching schools", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun filterSchools(status: String) {
        val filtered = if (status == "All") {
            allSchools
        } else {
            allSchools.filter { it.schoolStatus.equals(status, ignoreCase = true) }
        }
        adapter.updateData(filtered.toMutableList())
    }

    private fun showSchoolDetailsDialog(school: PendingSchool) {
        val dialog = AdminPendingSchoolDetailsDialogFragment.newInstance(school)
        dialog.show(supportFragmentManager, "SchoolDetailsDialog")
    }

    // 🔄 Refresh list when school status is updated
    override fun onSchoolUpdated(schoolId: Int, newStatus: String) {
        allSchools.find { it.id == schoolId }?.schoolStatus = newStatus
        filterSchools(selectedStatus) // re-apply current filter
    }
}
