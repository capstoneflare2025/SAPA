package com.example.sapa

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.sapa.databinding.FragmentAdminHomeBinding
import org.json.JSONObject

class AdminHomeFragment : Fragment() {
    private var _binding: FragmentAdminHomeBinding? = null
    private val binding get() = _binding!!

    private val baseUrl = "http://192.168.254.193/sapa_api" // change to your server IP/folder

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHomeBinding.inflate(inflater, container, false)

        // Load counts for schools
        loadSchoolCounts()
        loadCoordinatorCounts()

        // Dashboard card clicks
        binding.cardAddSchool.setOnClickListener {
            val intent = Intent(requireContext(), AdminHomePendingSchoolsActivity::class.java)
            startActivity(intent)
        }

        binding.cardAddStudent.setOnClickListener {
            val intent = Intent(requireContext(), AdminHomePendingCoordinatorsActivity::class.java)
            startActivity(intent)
        }

        binding.cardAppointments.setOnClickListener {
            Toast.makeText(requireContext(), "Appointments clicked", Toast.LENGTH_SHORT).show()
            // TODO: navigate to appointments activity
        }

        binding.cardBilling.setOnClickListener {
            Toast.makeText(requireContext(), "Billing clicked", Toast.LENGTH_SHORT).show()
            // TODO: navigate to billing activity
        }

        return binding.root
    }

    /** ------------------ Load School Counts ------------------ */
    private fun loadSchoolCounts() {
        val url = "$baseUrl/add_school/get_school_counts.php"

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val obj = JSONObject(response)

                    if (obj.getString("response") == "success") {
                        val data = obj.getJSONObject("data")

                        val total = data.getInt("total")
                        val pending = data.getInt("pending")
                        val approved = data.getInt("approved")
                        val declined = data.getInt("declined")

                        binding.tvSchoolCount.text = "Total: $total"
                        binding.tvSchoolPending.text = "Pending: $pending"
                        binding.tvSchoolApproved.text = "Approved: $approved"
                        binding.tvSchoolDeclined.text = "Declined: $declined"
                    } else {
                        Toast.makeText(requireContext(), "Server error: ${obj.optString("message")}", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Parse error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(requireContext()).add(request)
    }

    /** ------------------ Load Coordinator Counts ------------------ */
    private fun loadCoordinatorCounts() {
        val url = "$baseUrl/coordinators/get_coordinator_counts.php"

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val obj = JSONObject(response)

                    if (obj.getString("response") == "success") {
                        val data = obj.getJSONObject("data")

                        val total = data.getInt("total")
                        val pending = data.getInt("pending")
                        val approved = data.getInt("approved")
                        val declined = data.getInt("declined")

                        binding.tvCoordinatorCount.text = "Total: $total"
                        binding.tvCoordinatorPending.text = "Pending: $pending"
                        binding.tvCoordinatorApproved.text = "Approved: $approved"
                        binding.tvCoordinatorDeclined.text = "Declined: $declined"
                    } else {
                        Toast.makeText(requireContext(), "Server error: ${obj.optString("message")}", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Parse error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(requireContext()).add(request)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
