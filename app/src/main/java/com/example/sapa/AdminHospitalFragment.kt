package com.example.sapa

import android.content.Intent
import android.os.Bundle
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
    }

    private fun fetchHospitals() {
        thread {
            try {
                val url = URL("http://192.168.254.193/sapa_api/add_hospital/get_hospitals.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                val data = conn.inputStream.bufferedReader().readText()
                val jsonArray = JSONArray(data)

                val hospitals = mutableListOf<AdminHospital>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    hospitals.add(
                        AdminHospital(
                            obj.getInt("id"),
                            obj.getString("hospital_name")
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
