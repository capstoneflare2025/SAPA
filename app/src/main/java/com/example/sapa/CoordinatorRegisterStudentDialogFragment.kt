package com.example.sapa

import android.app.DatePickerDialog
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
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.sapa.databinding.DialogCoordinatorRegisterStudentBinding
import com.google.android.material.textfield.TextInputEditText
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*

class CoordinatorRegisterStudentDialogFragment : DialogFragment() {

    private var _binding: DialogCoordinatorRegisterStudentBinding? = null
    private val binding get() = _binding!!
    private val client = OkHttpClient()

    interface OnStudentAddedListener {
        fun onStudentAdded(newStudent: Students)
    }

    interface OnStudentUpdatedListener {
        fun onStudentUpdated(updatedStudent: Students)
    }

    private var addListener: OnStudentAddedListener? = null
    private var updateListener: OnStudentUpdatedListener? = null

    fun setOnStudentAddedListener(listener: OnStudentAddedListener) {
        this.addListener = listener
    }

    fun setOnStudentUpdatedListener(listener: OnStudentUpdatedListener) {
        this.updateListener = listener
    }

    private lateinit var spinnerDialog: Dialog
    private var spinnerMessageView: TextView? = null

    private var isEditMode = false
    private var editStudentId: Int = -1

    companion object {
        fun newInstance(student: Students?): CoordinatorRegisterStudentDialogFragment {
            val fragment = CoordinatorRegisterStudentDialogFragment()
            val args = Bundle()
            student?.let {
                args.putInt("id", it.id)
                args.putString("first_name", it.studentFirstName)
                args.putString("middle_name", it.studentMiddleName)
                args.putString("last_name", it.studentLastName)
                args.putString("school_name", it.schoolName)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        _binding = DialogCoordinatorRegisterStudentBinding.inflate(inflater, container, false)

        // Check if editing
        arguments?.let {
            if (it.containsKey("id")) {
                isEditMode = true
                editStudentId = it.getInt("id")
                binding.studentFirstName.setText(it.getString("first_name"))
                binding.studentMiddleName.setText(it.getString("middle_name"))
                binding.studentLastName.setText(it.getString("last_name"))
                binding.schoolName.setText(it.getString("school_name"), false)
                binding.btnAdd.text = "Update"
            }
        }

        // Back & Add/Update buttons
        binding.btnBack.setOnClickListener { dismiss() }
        binding.btnAdd.setOnClickListener {
            if (isEditMode) updateStudent() else saveStudent()
        }

        // Auto format names
        formatField(binding.studentFirstName)
        formatField(binding.studentMiddleName)
        formatField(binding.studentLastName)

        // Contact number filter
        binding.studentContact.filters = arrayOf(InputFilter.LengthFilter(11))

        // Email validation
        binding.studentEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()
                if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.studentEmail.error = null
                } else {
                    binding.studentEmail.error = "Invalid email"
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Contact validation
        binding.studentContact.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val contact = s.toString().trim()
                if (!contact.matches(Regex("^09[0-9]{9}$"))) {
                    binding.studentContact.error = "Must start with 09 and be 11 digits"
                } else {
                    binding.studentContact.error = null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Birthday picker
        binding.studentBirthday.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(requireContext(), { _, y, m, d ->
                val formattedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                binding.studentBirthday.setText(formattedDate)
            }, year, month, day)

            datePicker.show()
        }

        // Gender dropdown
        val studentGenders = listOf("Male", "Female", "Other")
        val studentGenderAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, studentGenders)
        binding.studentGender.setAdapter(studentGenderAdapter)

        binding.studentGender.setOnItemClickListener { _, _, position, _ ->
            if (studentGenders[position] == "Other") {
                binding.studentOtherGender.visibility = View.VISIBLE
            } else {
                binding.studentOtherGender.visibility = View.GONE
            }
        }

        // Spinner dialog
        createSpinnerDialog()
        fetchApprovedSchools()

        return binding.root
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
        spinnerDialog = Dialog(requireContext())
        spinnerDialog.setContentView(R.layout.coordinator_dialog_spinner)
        spinnerDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        spinnerDialog.setCancelable(false)
        spinnerDialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        spinnerDialog.window?.setGravity(Gravity.CENTER)
        spinnerMessageView = spinnerDialog.findViewById(R.id.progressMessage)
    }

    private fun showSpinner(message: String) {
        spinnerMessageView?.text = message
        spinnerDialog.show()
    }

    private fun fetchApprovedSchools() {
        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/add_school/get_schools.php")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    if (spinnerDialog.isShowing) spinnerDialog.dismiss()
                    Toast.makeText(requireContext(), "Failed to load schools", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                requireActivity().runOnUiThread {
                    if (!responseBody.isNullOrEmpty()) {
                        try {
                            val schools = mutableListOf<String>()
                            val jsonArray = org.json.JSONArray(responseBody)

                            val sharedPref = requireActivity().getSharedPreferences("SAPA_PREFS", Context.MODE_PRIVATE)
                            val sessionCoordinatorEmail = sharedPref.getString("coordinator_email", "") ?: ""

                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                val name = obj.getString("school_name")
                                val status = obj.getString("school_status")
                                val coordinatorEmail = obj.optString("coordinator_email", "")

                                if (status.equals("approved", ignoreCase = true) &&
                                    coordinatorEmail.equals(sessionCoordinatorEmail, ignoreCase = true)) {
                                    schools.add(name)
                                }
                            }

                            if (schools.isNotEmpty()) {
                                showSpinner("Loading schools...")
                                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, schools)
                                binding.schoolName.setAdapter(adapter)
                                if (spinnerDialog.isShowing) spinnerDialog.dismiss()
                            } else {
                                showSpinner("Checking schools...")
                                Handler(Looper.getMainLooper()).postDelayed({
                                    if (spinnerDialog.isShowing) {
                                        spinnerDialog.dismiss()
                                        Toast.makeText(requireContext(), "No schools available", Toast.LENGTH_LONG).show()
                                        dismiss()
                                    }
                                }, 5000)
                            }
                        } catch (e: Exception) {
                            if (spinnerDialog.isShowing) spinnerDialog.dismiss()
                            Toast.makeText(requireContext(), "Invalid school data", Toast.LENGTH_SHORT).show()
                            dismiss()
                        }
                    } else {
                        if (spinnerDialog.isShowing) spinnerDialog.dismiss()
                        Toast.makeText(requireContext(), "Empty response from server", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                }
            }
        })
    }

    private fun saveStudent() {
        val studentFirstName = binding.studentFirstName.text.toString().trim()
        val studentMiddleName = binding.studentMiddleName.text.toString().trim()
        val studentLastName = binding.studentLastName.text.toString().trim()
        val studentBirthday = binding.studentBirthday.text.toString().trim()
        val studentGender = binding.studentGender.text.toString().trim()
        val studentOtherGender = binding.etOtherGender.text.toString().trim()
        val schoolName = binding.schoolName.text.toString().trim()
        val studentEmail = binding.studentEmail.text.toString().trim()
        val studentContact = binding.studentContact.text.toString().trim()

        if (studentFirstName.isEmpty() || studentLastName.isEmpty() || studentBirthday.isEmpty() ||
            studentGender.isEmpty() || schoolName.isEmpty() ||
            studentEmail.isEmpty() || studentContact.isEmpty()
        ) {
            Toast.makeText(requireContext(), "All required fields must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(studentEmail).matches()) {
            Toast.makeText(requireContext(), "Invalid email format", Toast.LENGTH_SHORT).show()
            return
        }

        if (!studentContact.matches(Regex("^09[0-9]{9}$"))) {
            Toast.makeText(requireContext(), "Contact must start with 09 and be 11 digits", Toast.LENGTH_SHORT).show()
            return
        }

        val studentFinalGender = if (studentGender == "Other") studentOtherGender else studentGender
        showSpinner("Adding student...")

        val sharedPref = requireActivity().getSharedPreferences("SAPA_PREFS", Context.MODE_PRIVATE)
        val coordinatorEmail = sharedPref.getString("coordinator_email", "") ?: ""

        val formBody = FormBody.Builder()
            .add("student_first_name", studentFirstName)
            .add("student_middle_name", studentMiddleName)
            .add("student_last_name", studentLastName)
            .add("student_birthday", studentBirthday)
            .add("student_gender", studentFinalGender)
            .add("school_name", schoolName)
            .add("student_email", studentEmail)
            .add("student_contact", studentContact)
            .add("coordinator_email", coordinatorEmail)
            .build()

        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/add_student/add_student.php")
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
                        try {
                            val json = JSONObject(responseBody)
                            val message = json.getString("message")

                            if (json.getString("response") == "success") {
                                val student = Students(
                                    id = json.getInt("id"),
                                    studentFirstName = studentFirstName,
                                    studentMiddleName = studentMiddleName,
                                    studentLastName = studentLastName,
                                    schoolName = schoolName
                                )
                                addListener?.onStudentAdded(student)

                                Handler(Looper.getMainLooper()).postDelayed({
                                    spinnerDialog.dismiss()
                                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                                    dismiss()
                                }, 2000)
                            } else {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                                spinnerDialog.dismiss()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Invalid response", Toast.LENGTH_SHORT).show()
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

    private fun updateStudent() {
        val studentFirstName = binding.studentFirstName.text.toString().trim()
        val studentMiddleName = binding.studentMiddleName.text.toString().trim()
        val studentLastName = binding.studentLastName.text.toString().trim()
        val schoolName = binding.schoolName.text.toString().trim()

        if (studentFirstName.isEmpty() || studentLastName.isEmpty() || schoolName.isEmpty()) {
            Toast.makeText(requireContext(), "Required fields cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        showSpinner("Updating student...")

        val formBody = FormBody.Builder()
            .add("id", editStudentId.toString())
            .add("student_first_name", studentFirstName)
            .add("student_middle_name", studentMiddleName)
            .add("student_last_name", studentLastName)
            .add("school_name", schoolName)
            .build()

        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/add_student/update_student.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    spinnerDialog.dismiss()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                requireActivity().runOnUiThread {
                    spinnerDialog.dismiss()
                    try {
                        val json = JSONObject(responseBody ?: "")
                        if (json.getString("response") == "success") {
                            val updated = Students(
                                id = editStudentId,
                                studentFirstName = studentFirstName,
                                studentMiddleName = studentMiddleName,
                                studentLastName = studentLastName,
                                schoolName = schoolName
                            )
                            updateListener?.onStudentUpdated(updated)
                            Toast.makeText(requireContext(), "Student updated", Toast.LENGTH_SHORT).show()
                            dismiss()
                        } else {
                            Toast.makeText(requireContext(), json.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Invalid server response", Toast.LENGTH_SHORT).show()
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
