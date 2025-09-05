package com.example.sapa

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sapa.databinding.FragmentCoordinatorProfileBinding
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class CoordinatorProfileFragment : Fragment() {

    private var _binding: FragmentCoordinatorProfileBinding? = null
    private val binding get() = _binding!!

    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoordinatorProfileBinding.inflate(inflater, container, false)

        fetchUserProfile()
        fetchSchoolCount()
        fetchStudentCount()

        binding.btnLogout.setOnClickListener {
            logoutUser()
        }

        binding.moreInfo.setOnClickListener {
            val intent = Intent(requireContext(), CoordinatorProfileInformationActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    private fun fetchUserProfile() {
        val sharedPref = requireActivity().getSharedPreferences("SAPA_PREFS", android.content.Context.MODE_PRIVATE)
        val sessionEmail = sharedPref.getString("coordinator_email", null)

        if (sessionEmail.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No session found", Toast.LENGTH_SHORT).show()
            return
        }

        val formBody = FormBody.Builder()
            .add("coordinator_email", sessionEmail)
            .build()

        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/add_coordinator/get_profile.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body?.string()
                val json = JSONObject(res ?: "{}")

                requireActivity().runOnUiThread {
                    binding.coordinatorName.text =
                        "${json.optString("coordinator_first_name")} ${json.optString("coordinator_last_name")}".trim()
                    binding.coordinatorEmail.text = json.optString("coordinator_email")
                }
            }
        })
    }

    private fun fetchSchoolCount() {
        val sharedPref = requireActivity().getSharedPreferences("SAPA_PREFS", android.content.Context.MODE_PRIVATE)
        val coordinatorEmailSession = sharedPref.getString("coordinator_email", null)

        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/add_school/get_schools.php")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to load schools", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
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

                        requireActivity().runOnUiThread {
                            binding.textSchoolCount.text = count.toString()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    private fun fetchStudentCount() {
        val sharedPref = requireActivity().getSharedPreferences("SAPA_PREFS", android.content.Context.MODE_PRIVATE)
        val coordinatorEmailSession = sharedPref.getString("coordinator_email", null)

        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/add_school/get_students.php")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to load students", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
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

                        requireActivity().runOnUiThread {
                            // Optional: Add this TextView to your layout if not present
                            binding.textStudentsCount?.text = count.toString()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    private fun logoutUser() {
        val sharedPref = requireActivity().getSharedPreferences("SAPA_PREFS", android.content.Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("coordinator_email")
            remove("coordinator_id")
            remove("coordinator_first_name")
            remove("coordinator_middle_name")
            remove("coordinator_last_name")
            remove("coordinator_birthday")
            remove("coordinator_gender")
            remove("coordinator_contact")
            remove("coordinator_username")
            remove("coordinator_status")
            apply()
        }

        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
