package com.example.sapa

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.sapa.databinding.DialogPendingCoordinatorDetailsBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class AdminPendingCoordinatorDetailsDialogFragment : DialogFragment() {

    private var _binding: DialogPendingCoordinatorDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var coordinator: Coordinator
    private var listener: OnCoordinatorUpdatedListener? = null

    interface OnCoordinatorUpdatedListener {
        fun onCoordinatorUpdated(coordinatorId: Int, newStatus: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnCoordinatorUpdatedListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            coordinator = Coordinator(
                it.getInt("id"),
                it.getString("coordinator_username") ?: "",
                it.getString("coordinator_email") ?: "",
                it.getString("coordinator_first_name") ?: "",
                it.getString("coordinator_middle_name") ?: "",
                it.getString("coordinator_last_name") ?: "",
                it.getString("coordinator_contact") ?: "",
                it.getString("coordinator_birthday") ?: "",
                it.getString("coordinator_gender") ?: "",
                it.getString("coordinator_status") ?: "Pending",
            )
        }
        setStyle(STYLE_NO_FRAME, R.style.CustomDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPendingCoordinatorDetailsBinding.inflate(inflater, container, false)

        // 🔹 Bind details to UI
        binding.coordinatorFirstName.setText(coordinator.coordinatorFirstName)
        binding.coordinatorMiddleName.setText(coordinator.coordinatorMiddleName)
        binding.coordinatorLastName.setText(coordinator.coordinatorLastName)
        binding.coordinatorContact.setText(coordinator.coordinatorContact)
        binding.coordinatorBirthday.setText(coordinator.coordinatorBirthday)
        binding.coordinatorGender.setText(coordinator.coordinatorGender)
        binding.coordinatorEmail.setText(coordinator.coordinatorEmail)
        binding.coordinatorUserName.setText(coordinator.coordinatorUsername)

        // Make fields read-only
        listOf(
            binding.coordinatorFirstName,
            binding.coordinatorMiddleName,
            binding.coordinatorLastName,
            binding.coordinatorContact,
            binding.coordinatorBirthday,
            binding.coordinatorGender,
            binding.coordinatorEmail,
            binding.coordinatorUserName
        ).forEach { editText ->
            editText.isFocusable = false
            editText.isClickable = false
            editText.isLongClickable = false
            editText.keyListener = null
        }

        if (coordinator.coordinatorStatus.equals("pending", ignoreCase = true)) {
            binding.btnAdd.text = "Approve"
            binding.btnBack.text = "Decline"
            binding.btnAdd.setOnClickListener { updateCoordinatorStatus("approved") }
            binding.btnBack.setOnClickListener { updateCoordinatorStatus("rejected") }
        } else {
            binding.btnAdd.visibility = View.GONE
            binding.btnBack.visibility = View.GONE
        }

        binding.btnClose.setOnClickListener { dismiss() }

        return binding.root
    }

    private fun updateCoordinatorStatus(newStatus: String) {
        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("id", coordinator.id.toString())
            .add("coordinator_status", newStatus) // ✅ matches DB
            .build()

        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/add_coordinator/update_coordinator_status.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error updating coordinator", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()

                requireActivity().runOnUiThread {
                    Log.d("ServerResponse", "Response body: $body") // ✅ Add this line

                    if (body != null && body.trim().startsWith("{")) {
                        try {
                            val json = JSONObject(body)
                            if (json.optString("status") == "success") {
                                showResultDialog(newStatus)
                                listener?.onCoordinatorUpdated(coordinator.id, newStatus)
                            } else {
                                Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(requireContext(), "Invalid JSON from server", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Invalid server response", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        })
    }

    private fun showResultDialog(newStatus: String) {
        val layout = if (newStatus.equals("approved", ignoreCase = true)) {
            R.layout.dialog_pending_school_result_approved
        } else {
            R.layout.dialog_pending_school_result_declined
        }

        val dialogView = layoutInflater.inflate(layout, null)
        val alert = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        alert.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alert.show()

        dialogView.postDelayed({
            alert.dismiss()
            dismiss()
        }, 5000)
    }

    companion object {
        fun newInstance(coordinator: Coordinator): AdminPendingCoordinatorDetailsDialogFragment {
            val fragment = AdminPendingCoordinatorDetailsDialogFragment()
            fragment.arguments = Bundle().apply {
                putInt("id", coordinator.id)
                putString("coordinator_username", coordinator.coordinatorUsername)
                putString("coordinator_email", coordinator.coordinatorEmail)
                putString("coordinator_first_name", coordinator.coordinatorFirstName)
                putString("coordinator_middle_name", coordinator.coordinatorMiddleName)
                putString("coordinator_last_name", coordinator.coordinatorLastName)
                putString("coordinator_contact", coordinator.coordinatorContact)
                putString("coordinator_birthday", coordinator.coordinatorBirthday)
                putString("coordinator_gender", coordinator.coordinatorGender)
                putString("coordinator_status", coordinator.coordinatorStatus)
            }
            return fragment
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(), // 90% of screen width
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
