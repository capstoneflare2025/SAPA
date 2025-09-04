package com.example.sapa

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Patterns
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.sapa.databinding.DialogCoordinatorRegisterSchoolBinding
import com.google.android.material.textfield.TextInputEditText
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*

class CoordinatorRegisterSchoolDialogFragment : DialogFragment() {

    private var _binding: DialogCoordinatorRegisterSchoolBinding? = null
    private val binding get() = _binding!!
    private val client = OkHttpClient()

    private var selectedSchool: School? = null
    private var isEditMode = false

    interface OnSchoolAddedListener {
        fun onSchoolAdded(newSchool: School)
    }

    private var listener: OnSchoolAddedListener? = null

    fun setOnSchoolAddedListener(listener: OnSchoolAddedListener) {
        this.listener = listener
    }

    interface OnSchoolUpdatedListener {
        fun onSchoolUpdated(updatedSchool: School)
    }

    private var updateListener: OnSchoolUpdatedListener? = null

    fun setOnSchoolUpdatedListener(listener: OnSchoolUpdatedListener) {
        this.updateListener = listener
    }


    // Spinner view
    private lateinit var spinnerDialog: Dialog

    companion object {
        fun newInstance(school: School): CoordinatorRegisterSchoolDialogFragment {
            val fragment = CoordinatorRegisterSchoolDialogFragment()
            val args = Bundle()
            args.putInt("id", school.id)
            args.putString("school_name", school.schoolName)
            args.putString("school_status", school.schoolStatus)
            args.putString("school_email", school.schoolEmail)
            args.putString("school_contact", school.schoolContact)
            args.putString("school_street", school.schoolStreet)
            args.putString("school_city", school.schoolCity)
            args.putString("school_province", school.schoolProvince)
            args.putString("school_address", school.schoolAddress)
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedSchool = School(
                id = it.getInt("id"),
                schoolName = it.getString("school_name", ""),
                schoolStatus = it.getString("school_status", "Pending"),
                schoolEmail = it.getString("school_email", ""),
                schoolContact = it.getString("school_contact", ""),
                schoolStreet = it.getString("school_street", ""),
                schoolCity = it.getString("school_city", ""),
                schoolProvince = it.getString("school_province", ""),
                schoolAddress = it.getString("school_address", "")
            )
        }
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCoordinatorRegisterSchoolBinding.inflate(inflater, container, false)

        binding.btnBack.setOnClickListener { dismiss() }
        binding.btnAdd.setOnClickListener { saveSchool() }

        if (selectedSchool == null) {
            // Add Mode
            binding.btnAdd.text = "Add"
            binding.btnAdd.setOnClickListener { saveSchool() }
        } else {
            // Details Mode
            showSchoolDetails()
        }

        // ✅ Auto-format school name, street, city, and province
        formatField(binding.schoolName)
        formatField(binding.schoolStreet)
        formatField(binding.schoolCity)
        formatField(binding.schoolProvince)

        // ✅ Set InputFilter to limit the input to 11 digits only for contact number
        val filter = InputFilter.LengthFilter(11)
        binding.schoolContact.filters = arrayOf(filter)

        // ✅ Realtime school_email validation
        binding.schoolEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val schoolEmail = s.toString().trim()
                if (schoolEmail.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(schoolEmail).matches()) {
                    binding.schoolEmail.error = null
                } else {
                    binding.schoolEmail.error = "Invalid email"
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ✅ Realtime school_contact validation
        binding.schoolContact.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val contact = s.toString().trim()
                if (!contact.matches(Regex("^09[0-9]{9}\$"))) {
                    binding.schoolContact.error = "Must start with 09 and be 11 digits"
                } else {
                    binding.schoolContact.error = null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Spinner dialog
        createSpinnerDialog()

        return binding.root
    }


    private fun showSchoolDetails() {
        binding.schoolName.setText(selectedSchool?.schoolName)
        binding.schoolEmail.setText(selectedSchool?.schoolEmail)
        binding.schoolContact.setText(selectedSchool?.schoolContact)
        binding.schoolStreet.setText(selectedSchool?.schoolStreet)
        binding.schoolCity.setText(selectedSchool?.schoolCity)
        binding.schoolProvince.setText(selectedSchool?.schoolProvince)

        // Status (if you want it visible when viewing)
        binding.etSchoolStatusLayout.visibility = View.VISIBLE
        binding.etSchoolStatus.setText(selectedSchool?.schoolStatus)

        setFieldsEditable(false)
        binding.btnAdd.text = "Update"

        binding.btnAdd.setOnClickListener {
            if (!isEditMode) {
                // Switch to edit mode
                setFieldsEditable(true)
                binding.btnAdd.text = "Save"
                isEditMode = true
            } else {
                // Save update
                updateSchool()
            }
        }
    }


    private fun setFieldsEditable(enabled: Boolean) {
        binding.schoolName.isEnabled = enabled
        binding.schoolEmail.isEnabled = enabled
        binding.schoolContact.isEnabled = enabled
        binding.schoolStreet.isEnabled = enabled
        binding.schoolCity.isEnabled = enabled
        binding.schoolProvince.isEnabled = enabled
        binding.etSchoolStatus.isEnabled = false // keep status read-only
    }


    private fun updateSchool() {
        val id = selectedSchool?.id ?: return
        val schoolName = binding.schoolName.text.toString().trim()
        val schoolEmail = binding.schoolEmail.text.toString().trim()
        val schoolContact = binding.schoolContact.text.toString().trim()
        val schoolStreet = binding.schoolStreet.text.toString().trim()
        val schoolCity = binding.schoolCity.text.toString().trim()
        val schoolProvince = binding.schoolProvince.text.toString().trim()

        val schoolAddress = "$schoolStreet, $schoolCity, $schoolProvince"

        val formBodyBuilder = FormBody.Builder()
            .add("id", id.toString())

        if (schoolName.isNotEmpty()) formBodyBuilder.add("school_name", schoolName)
        if (schoolEmail.isNotEmpty()) formBodyBuilder.add("school_email", schoolEmail)
        if (schoolContact.isNotEmpty()) formBodyBuilder.add("school_contact", schoolContact)
        if (schoolStreet.isNotEmpty()) formBodyBuilder.add("school_street", schoolStreet)
        if (schoolCity.isNotEmpty()) formBodyBuilder.add("school_city", schoolCity)
        if (schoolProvince.isNotEmpty()) formBodyBuilder.add("school_province", schoolProvince)
        if (schoolAddress.isNotEmpty()) formBodyBuilder.add("school_address", schoolAddress)

        val formBody = formBodyBuilder.build()

        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/add_school/update_school_details.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                requireActivity().runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "School updated", Toast.LENGTH_SHORT).show()

                        // 👇 Notify parent fragment/activity
                        val updated = selectedSchool?.copy(
                            schoolName = schoolName,
                            schoolEmail = schoolEmail,
                            schoolContact = schoolContact,
                            schoolStreet = schoolStreet,
                            schoolCity = schoolCity,
                            schoolProvince = schoolProvince,
                            schoolAddress = schoolAddress
                        )
                        if (updated != null) updateListener?.onSchoolUpdated(updated)

                        dismiss()
                    } else {
                        Toast.makeText(requireContext(), body ?: "Update failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }



    private fun formatField(field: TextInputEditText) {
        field.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    val formatted = s.toString().lowercase(Locale.getDefault())
                        .split(" ")
                        .joinToString(" ") { word ->
                            word.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                            }
                        }
                    if (formatted != s.toString()) {
                        field.removeTextChangedListener(this)
                        field.setText(formatted)
                        field.setSelection(formatted.length)
                        field.addTextChangedListener(this)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun createSpinnerDialog() {
        // Spinner dialog with a card view
        spinnerDialog = Dialog(requireContext())
        spinnerDialog.setContentView(R.layout.coordinator_dialog_spinner) // A layout with a CardView and "Adding school..." text
        spinnerDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        spinnerDialog.setCancelable(false)
    }

    private fun saveSchool() {
        val schoolName = binding.schoolName.text.toString().trim()
        val schoolEmail = binding.schoolEmail.text.toString().trim()
        val schoolContact = binding.schoolContact.text.toString().trim()
        val schoolStreet = binding.schoolStreet.text.toString().trim()
        val schoolCity = binding.schoolCity.text.toString().trim()
        val schoolProvince = binding.schoolProvince.text.toString().trim()

        // Normalize the address (remove extra spaces)
        val normalizedSchoolStreet = schoolStreet.replace(Regex("\\s+"), " ").trim()
        val normalizedSchoolCity = schoolCity.replace(Regex("\\s+"), " ").trim()
        val normalizedSchoolProvince = schoolProvince.replace(Regex("\\s+"), " ").trim()

        // Combine normalized address
        val schoolFullAddress = "$normalizedSchoolStreet, $normalizedSchoolCity, $normalizedSchoolProvince"

        if (schoolName.isEmpty() || schoolEmail.isEmpty() || schoolContact.isEmpty()
            || normalizedSchoolStreet.isEmpty() || normalizedSchoolCity.isEmpty() || normalizedSchoolProvince.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(schoolEmail).matches()) {
            Toast.makeText(requireContext(), "Invalid email format", Toast.LENGTH_SHORT).show()
            return
        }

        if (!schoolContact.matches(Regex("^09[0-9]{9}\$"))) {
            Toast.makeText(requireContext(), "Contact must start with 09 and be 11 digits", Toast.LENGTH_SHORT).show()
            return
        }

        // Show the spinner dialog (Adding school...)
        spinnerDialog.show()

        // Proceed with saving the school data
        val sharedPref = requireActivity().getSharedPreferences("SAPA_PREFS", Context.MODE_PRIVATE)
        val coordinatorEmail = sharedPref.getString("coordinator_email", "") ?: ""
        val coordinatorFirstname = sharedPref.getString("coordinator_first_name", "") ?: ""
        val coordinatorMiddlename = sharedPref.getString("coordinator_middle_name", "") ?: ""
        val coordinatorLastname = sharedPref.getString("coordinator_last_name", "") ?: ""
        val coordinatorName = listOf(coordinatorFirstname, coordinatorMiddlename, coordinatorLastname)
            .filter { it.isNotEmpty() }
            .joinToString(" ")

        val formBody = FormBody.Builder()
            .add("school_name", schoolName)
            .add("school_email", schoolEmail)
            .add("school_contact", schoolContact)
            .add("school_street", normalizedSchoolStreet)
            .add("school_city", normalizedSchoolCity)
            .add("school_province", normalizedSchoolProvince)
            .add("school_address", schoolFullAddress)
            .add("coordinator_email", coordinatorEmail)
            .add("coordinator_name", coordinatorName)
            .add("school_status", "Pending") // 👈 add this
            .build()


        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/add_school/add_school.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                    spinnerDialog.dismiss()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                requireActivity().runOnUiThread {
                    if (!responseBody.isNullOrEmpty()) {
                        // 👇 debug print
                        println("🔍 Server response: $responseBody")

                        try {
                            val json = JSONObject(responseBody)
                            val message = json.getString("message")

                            if (json.getString("response") == "success") {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    spinnerDialog.dismiss()
                                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                                    dismiss()
                                }, 3000)
                            } else {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                                spinnerDialog.dismiss()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Invalid response: $responseBody", Toast.LENGTH_LONG).show()
                            spinnerDialog.dismiss()
                        }
                    } else {
                        Toast.makeText(requireContext(), "No response from server", Toast.LENGTH_SHORT).show()
                        spinnerDialog.dismiss()
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
