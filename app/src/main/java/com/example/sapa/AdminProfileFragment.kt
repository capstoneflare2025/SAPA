package com.example.sapa

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.sapa.databinding.FragmentAdminProfileBinding
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AdminProfileFragment : Fragment() {
    private var _binding: FragmentAdminProfileBinding? = null
    private val binding get() = _binding!!

    private val baseUrl = "http://192.168.254.193/sapa_api" // Update as needed

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminProfileBinding.inflate(inflater, container, false)

        // Load admin email from SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("SAPA_PREFS", AppCompatActivity.MODE_PRIVATE)
        val adminEmail = sharedPref.getString("admin_email", null)

        if (adminEmail != null) {
            fetchAdminProfile(adminEmail)
        } else {
            Toast.makeText(requireContext(), "Admin not logged in", Toast.LENGTH_SHORT).show()
        }

        loadSchoolCounts()
        loadCoordinatorCounts()


        // Logout button
        binding.btnLogout.setOnClickListener {
            with(sharedPref.edit()) {
                remove("admin_email")
                apply()
            }
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        return binding.root
    }

    private fun fetchAdminProfile(email: String) {
        Thread {
            try {
                val url = URL("http://192.168.254.193/sapa_api/add_coordinator/get_admin_profile.php")
                val postData = "email=$email"

                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.outputStream.write(postData.toByteArray(Charsets.UTF_8))

                val response = conn.inputStream.bufferedReader().readText()

                val json = JSONObject(response)
                if (json.getString("status") == "success") {
                    val data = json.getJSONObject("data")
                    val fullName = data.getString("full_name")
                    val emailReturned = data.getString("email")

                    requireActivity().runOnUiThread {
                        binding.textAdminName.text = fullName
                        binding.textAdminEmail.text = emailReturned
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Admin data not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }


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
                        binding.textSchoolCount.text = "$total"
                    } else {
                        Toast.makeText(requireContext(), "Failed to load school count", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(requireContext(), "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun loadCoordinatorCounts() {
        val url = "$baseUrl/add_coordinator/get_coordinator_counts.php"

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val obj = JSONObject(response)
                    if (obj.getString("response") == "success") {
                        val data = obj.getJSONObject("data")
                        val total = data.getInt("total")
                        binding.textCoordinatorsCount.text = "$total"
                    } else {
                        Toast.makeText(requireContext(), "Failed to load coordinator count", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(requireContext(), "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(requireContext()).add(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
