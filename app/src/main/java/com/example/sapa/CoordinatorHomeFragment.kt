package com.example.sapa

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.sapa.databinding.FragmentCoordinatorHomeBinding
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class CoordinatorHomeFragment : Fragment() {
    private var _binding: FragmentCoordinatorHomeBinding? = null
    private val binding get() = _binding!!

    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoordinatorHomeBinding.inflate(inflater, container, false)
        setupClicks()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        fetchSchoolCount()
        fetchStudentCount()
        fetchAppointmentCount() // ✅ Add this line
    }

    private fun setupClicks() {
        binding.cardAddSchool.setOnClickListener {
            val intent = Intent(requireContext(), CoodinatorAddSchoolActivity::class.java)
            startActivity(intent)
        }

        binding.cardAddStudent.setOnClickListener {
            val intent = Intent(requireContext(), CoordinatorAddStudentActivity::class.java)
            startActivity(intent)
        }


        binding.cardAppointments.setOnClickListener {
            val intent = Intent(requireContext(), CoordinatorAppointmentListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchAppointmentCount() {
        val sharedPref = requireContext().getSharedPreferences("SAPA_PREFS", AppCompatActivity.MODE_PRIVATE)
        val coordinatorEmailSession = sharedPref.getString("coordinator_email", null)

        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/add_appointment/get_appointments.php")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to load appointments", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { body ->
                        try {
                            val jsonArray = JSONArray(body)
                            var count = 0

                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                val coordinatorEmail = obj.optString("coordinator_email", "")
                                if (coordinatorEmail.equals(coordinatorEmailSession, ignoreCase = true)) {
                                    count++
                                }
                            }

                            activity?.runOnUiThread {
                                binding.tvAppointmentCount.text = "$count Total"
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        })
    }


    private fun fetchSchoolCount() {
        val sharedPref = requireContext().getSharedPreferences("SAPA_PREFS", AppCompatActivity.MODE_PRIVATE)
        val coordinatorEmailSession = sharedPref.getString("coordinator_email", null)

        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/add_school/get_schools.php")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to load schools", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { body ->
                        try {
                            val jsonArray = JSONArray(body)
                            var count = 0

                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                val coordinatorEmail = obj.optString("coordinator_email", "")
                                if (coordinatorEmail.equals(coordinatorEmailSession, ignoreCase = true)) {
                                    count++
                                }
                            }

                            activity?.runOnUiThread {
                                binding.tvSchoolCount.text = "$count Total"
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        })
    }

    private fun fetchStudentCount() {
        val sharedPref = requireContext().getSharedPreferences("SAPA_PREFS", AppCompatActivity.MODE_PRIVATE)
        val coordinatorEmailSession = sharedPref.getString("coordinator_email", null)

        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/add_school/get_students.php")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to load students", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { body ->
                        try {
                            val jsonArray = JSONArray(body)
                            var count = 0

                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                val coordinatorEmail = obj.optString("coordinator_email", "")
                                if (coordinatorEmail.equals(coordinatorEmailSession, ignoreCase = true)) {
                                    count++
                                }
                            }

                            activity?.runOnUiThread {
                                binding.tvStudentCount.text = "$count Total"
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

