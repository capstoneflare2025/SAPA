package com.example.sapa

import AdminHospital
import AdminHospitalAdapter
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sapa.databinding.FragmentAdminHospitalBinding
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class AdminHospitalFragment : Fragment() {

    private var _binding: FragmentAdminHospitalBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AdminHospitalAdapter
    private var allHospitals = mutableListOf<AdminHospital>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminHospitalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        adapter = AdminHospitalAdapter(requireContext(), "admin_email@example.com", mutableListOf())
        binding.recyclerHospitals.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHospitals.adapter = adapter

        // Fetch hospitals
        fetchHospitals()

        // Open Activity when button clicked
        binding.btnAddHospital.setOnClickListener {
            val intent = Intent(requireActivity(), AdminAddHospitalActivity::class.java)
            startActivity(intent)
        }

        // Add TextWatcher for filtering by ID
        binding.searchId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterHospitalsById(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchHospitals() {
        thread {
            try {
                // Fetch hospitals from the server
                val url = URL("http://192.168.254.193/sapa_api/add_hospital/get_hospitals.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connect()

                // Check the HTTP status code
                val statusCode = conn.responseCode
                if (statusCode != HttpURLConnection.HTTP_OK) {
                    Log.e("HTTP Error", "Server returned error: $statusCode")
                    return@thread
                }

                // Read the server response
                val data = conn.inputStream.bufferedReader().readText()
                Log.d("HospitalData", "Response: $data")  // Log the raw data

                // Parse the JSON response
                val jsonArray = JSONArray(data)

                val hospitals = mutableListOf<AdminHospital>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)

                    // Fetch each hospital data from the JSON
                    val hospitalEmail = obj.getString("hospital_email")

                    // Fetch allocation data for the hospital using hospital_email
                    val allocationData = fetchAllocationData(hospitalEmail)

                    // Create AdminHospital object and add it to the list
                    hospitals.add(
                        AdminHospital(
                            obj.getInt("id"),
                            obj.getString("hospital_name"),
                            hospitalEmail,
                            obj.getString("hospital_contact"),
                            obj.getString("hospital_city"),
                            obj.getString("hospital_type"),
                            obj.getString("hospital_street"),
                            obj.getString("hospital_province"),
                            allocationData["allocation_name"] ?: "N/A",  // Use allocation data if available
                            allocationData["allocation_section"] ?: "N/A",
                            allocationData["time_slot"] ?: "N/A",
                            allocationData["billing_info"] ?: "N/A"  // Add billing info
                        )
                    )
                }

                // Update RecyclerView on UI thread
                activity?.runOnUiThread {
                    allHospitals = hospitals
                    adapter.updateData(allHospitals)  // Update adapter with the fetched list
                }

            } catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error fetching hospitals", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun fetchAllocationData(hospitalEmail: String): Map<String, String?> {
        val allocationData = mutableMapOf<String, String?>()

        try {
            val url = URL("http://192.168.254.193/sapa_api/add_hospital/get_allocation_billing.php?hospital_email=$hospitalEmail")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"

            val data = conn.inputStream.bufferedReader().readText()
            val jsonArray = JSONArray(data)

            if (jsonArray.length() > 0) {
                val allocationObj = jsonArray.getJSONObject(0)
                allocationData["allocation_name"] = allocationObj.optString("allocation_name")
                allocationData["allocation_section"] = allocationObj.optString("allocation_section")
                allocationData["time_slot"] = allocationObj.optString("time_slot")
                allocationData["billing_info"] = allocationObj.optString("billing_info")  // Fetching billing_info
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return allocationData
    }



    // Filter hospitals by ID based on search query
    private fun filterHospitalsById(query: String) {
        if (query.isEmpty()) {
            adapter.updateData(allHospitals)
        } else {
            val filteredList = allHospitals.filter {
                it.id.toString().contains(query)
            }
            adapter.updateData(filteredList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
