package com.example.sapa

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.sapa.databinding.DialogPendingSchoolDetailsBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class AdminPendingSchoolDetailsDialogFragment : DialogFragment() {

    private var _binding: DialogPendingSchoolDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var school: PendingSchool
    private var listener: OnSchoolUpdatedListener? = null

    interface OnSchoolUpdatedListener {
        fun onSchoolUpdated(schoolId: Int, newStatus: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnSchoolUpdatedListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            school = PendingSchool(
                it.getInt("id"),
                it.getString("schoolName") ?: "",
                it.getString("schoolEmail") ?: "",
                it.getString("schoolContact") ?: "",
                it.getString("schoolStreet") ?: "",
                it.getString("schoolCity") ?: "",
                it.getString("schoolProvince") ?: "",
                it.getString("schoolAddress") ?: "",
                it.getString("coordinatorName") ?: "",
                it.getString("coordinatorEmail") ?: "",
                it.getString("schoolStatus") ?: "Pending"
            )
        }
        // ✅ Use transparent dialog theme instead of fullscreen
        setStyle(STYLE_NO_FRAME, R.style.CustomDialog)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPendingSchoolDetailsBinding.inflate(inflater, container, false)

        // 🔹 Bind details to UI
        binding.schoolName.setText(school.schoolName)
        binding.schoolEmail.setText(school.schoolEmail)
        binding.schoolContact.setText(school.schoolContact)
        binding.schoolStreet.setText(school.schoolStreet)
        binding.schoolCity.setText(school.schoolCity)
        binding.schoolProvince.setText(school.schoolProvince)
        binding.coordinatorName.setText(school.coordinatorName)
        binding.coordinatorEmail.setText(school.coordinatorEmail)
        binding.schoolStatus.setText(school.schoolStatus)

        // 🔹 Make all fields read-only
        listOf(
            binding.schoolName,
            binding.schoolEmail,
            binding.schoolContact,
            binding.schoolStreet,
            binding.schoolCity,
            binding.schoolProvince,
            binding.coordinatorName,
            binding.coordinatorEmail,
            binding.schoolStatus
        ).forEach { editText ->
            editText.isFocusable = false
            editText.isClickable = false
            editText.isLongClickable = false
            editText.keyListener = null
        }

        // 🔹 Toggle buttons & close icon based on status
        if (school.schoolStatus.equals("Pending", ignoreCase = true)) {
            // Show Approve & Decline
            binding.btnAdd.text = "Approve"
            binding.btnBack.text = "Decline"
            binding.btnAdd.setOnClickListener { updateSchoolStatus("Approved") }
            binding.btnBack.setOnClickListener { updateSchoolStatus("Declined") }

        } else {
            // Hide Approve/Decline buttons
            binding.btnAdd.visibility = View.GONE
            binding.btnBack.visibility = View.GONE

        }


        // Close icon
        binding.btnClose.setOnClickListener { dismiss() }

        return binding.root
    }

    private fun updateSchoolStatus(newStatus: String) {
        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("id", school.id.toString())
            .add("school_status", newStatus)
            .build()

        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/add_school/update_school_status.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error updating school", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                requireActivity().runOnUiThread {
                    if (body != null && JSONObject(body).optString("status") == "success") {
                        showResultDialog(newStatus) // only show this
                        listener?.onSchoolUpdated(school.id, newStatus)
                    } else {
                        Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }


    private fun showResultDialog(newStatus: String) {
        val layout = if (newStatus.equals("Approved", ignoreCase = true)) {
            R.layout.dialog_pending_school_result_approved
        } else {
            R.layout.dialog_pending_school_result_declined
        }

        val dialogView = layoutInflater.inflate(layout, null)
        val alert = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // 🔹 Remove default white background
        alert.window?.setBackgroundDrawableResource(android.R.color.transparent)

        alert.show()

        dialogView.postDelayed({
            alert.dismiss()
            dismiss() // close main dialog
        }, 5000)
    }


    companion object {
        fun newInstance(school: PendingSchool): AdminPendingSchoolDetailsDialogFragment {
            val fragment = AdminPendingSchoolDetailsDialogFragment()
            fragment.arguments = Bundle().apply {
                putInt("id", school.id)
                putString("schoolName", school.schoolName)
                putString("schoolEmail", school.schoolEmail)
                putString("schoolContact", school.schoolContact)
                putString("schoolStreet", school.schoolStreet)
                putString("schoolCity", school.schoolCity)
                putString("schoolProvince", school.schoolProvince)
                putString("schoolAddress", school.schoolAddress)
                putString("coordinatorName", school.coordinatorName)
                putString("coordinatorEmail", school.coordinatorEmail)
                putString("schoolStatus", school.schoolStatus)
            }
            return fragment
        }
    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(), // 90% width
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
