package com.example.sapa

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sapa.databinding.ActivityAdminPendingCoordinatorsBinding
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class AdminHomePendingCoordinatorsActivity : AppCompatActivity(),
    AdminPendingCoordinatorDetailsDialogFragment.OnCoordinatorUpdatedListener {

    private lateinit var binding: ActivityAdminPendingCoordinatorsBinding
    private lateinit var adapter: UserAdapter
    private var allCoordinators = mutableListOf<Coordinator>()
    private var selectedStatus: String = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPendingCoordinatorsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Setup RecyclerView
        adapter = UserAdapter(
            coordinators = mutableListOf(),
            onItemClick = { coordinator -> showCoordinatorDetailsDialog(coordinator) }
        )

        binding.recyclerPendingCoordinators.layoutManager = LinearLayoutManager(this)
        binding.recyclerPendingCoordinators.adapter = adapter

        // ✅ Setup Dropdown filter
        val statuses = listOf("All", "Pending", "Approved", "Declined")
        val dropdownAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statuses)
        binding.filterSpinner.setAdapter(dropdownAdapter)

        binding.filterSpinner.setOnItemClickListener { _, _, position, _ ->
            selectedStatus = statuses[position]
            filterCoordinators(selectedStatus)
        }

        // ✅ Fetch coordinators
        fetchAllCoordinators()

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun fetchAllCoordinators() {
        thread {
            try {
                val url = URL("http://192.168.254.193/sapa_api/coordinators/get_all_coordinators.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                val data = conn.inputStream.bufferedReader().readText()
                val jsonArray = JSONArray(data)

                val coordinators = mutableListOf<Coordinator>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    coordinators.add(
                        Coordinator(
                            obj.getInt("id"),
                            obj.getString("coordinator_username"),
                            obj.getString("coordinator_email"),
                            obj.getString("coordinator_first_name"),
                            obj.getString("coordinator_middle_name"),
                            obj.getString("coordinator_last_name"),
                            obj.getString("coordinator_contact"),
                            obj.getString("coordinator_birthday"),
                            obj.getString("coordinator_gender"),
                            obj.getString("coordinator_status")
                        )
                    )
                }

                runOnUiThread {
                    allCoordinators = coordinators
                    adapter.updateList(allCoordinators)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Error fetching coordinators", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun filterCoordinators(status: String) {
        val filtered = if (status == "All") {
            allCoordinators
        } else {
            allCoordinators.filter { it.coordinatorStatus.equals(status, ignoreCase = true) }
        }
        adapter.updateList(filtered.toMutableList())
    }

    private fun showCoordinatorDetailsDialog(coordinator: Coordinator) {
        val dialog = AdminPendingCoordinatorDetailsDialogFragment.newInstance(coordinator)
        dialog.show(supportFragmentManager, "CoordinatorDetailsDialog")
    }

    // 🔄 Refresh list when coordinator status is updated
    override fun onCoordinatorUpdated(coordinatorId: Int, newStatus: String) {
        allCoordinators.find { it.id == coordinatorId }?.coordinatorStatus = newStatus
        filterCoordinators(selectedStatus)
    }

    private fun updateCoordinatorStatus(coordinator: Coordinator, newStatus: String) {
        // Optional shortcut: approve/decline without opening dialog
        val dialog = AdminPendingCoordinatorDetailsDialogFragment.newInstance(coordinator)
        dialog.show(supportFragmentManager, "CoordinatorDetailsDialog")
    }
}
