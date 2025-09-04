package com.example.sapa

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.sapa.databinding.ActivityRegisterBinding
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔹 Contact: digits only & max 11
        binding.coordinatorContact.inputType = InputType.TYPE_CLASS_NUMBER
        binding.coordinatorContact.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(11))

        // 🔹 Capitalize Names automatically
        setupNameCapitalization(binding.coordinatorFirstName)
        setupNameCapitalization(binding.coordinatorMiddleName)
        setupNameCapitalization(binding.coordinatorLastName)

        // 🔹 Setup Gender Dropdown
        val coordinatorGenderOptions = listOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, coordinatorGenderOptions)
        binding.coordinatorGender.setAdapter(adapter)

        binding.coordinatorGender.setOnItemClickListener { _, _, position, _ ->
            val selected = coordinatorGenderOptions[position]
            if (selected == "Other") {
                binding.layoutOtherGender.visibility = View.VISIBLE
            } else {
                binding.layoutOtherGender.visibility = View.GONE
                binding.coordinatorOtherGender.text?.clear()
            }
        }

        // 🔹 Setup Birthday Picker (MM/DD/YYYY, 1900–currentYear)
        binding.coordinatorBirthday.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formatted = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                    binding.coordinatorBirthday.setText(formatted)

                },
                year, month, day
            )

            // Limit years between 1900 and current year
            val minDate = Calendar.getInstance().apply { set(1900, 0, 1) }
            val maxDate = Calendar.getInstance().apply { set(year, 11, 31) }

            datePicker.datePicker.minDate = minDate.timeInMillis
            datePicker.datePicker.maxDate = maxDate.timeInMillis

            datePicker.show()
        }

        // 🔹 Live check (username, email, contact, password)
        setupLiveValidation()

        // Redirect to login
        binding.btnLoginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Handle register button
        binding.btnRegister.setOnClickListener {
            var coordinatorFirstName = binding.coordinatorFirstName.text.toString().trim()
            var coordinatorMiddleName = binding.coordinatorMiddleName.text.toString().trim()
            var coordinatorLastName = binding.coordinatorLastName.text.toString().trim()
            val coordinatorBirthday = binding.coordinatorBirthday.text.toString().trim()

            // Ensure proper capitalization before saving
            coordinatorFirstName = formatName(coordinatorFirstName)
            coordinatorMiddleName = formatName(coordinatorMiddleName)
            coordinatorLastName = formatName(coordinatorLastName)

            // 👇 Handle gender
            var coordinatorGender = binding.coordinatorGender.text.toString().trim()
            if (coordinatorGender == "Other") {
                val other = binding.coordinatorOtherGender.text.toString().trim()
                if (other.isEmpty()) {
                    showTopToast("Please specify gender")
                    return@setOnClickListener
                }
                coordinatorGender = other
            }

            val coordinatorContact = binding.coordinatorContact.text.toString().trim()
            val coordinatorEmail = binding.coordinatorEmail.text.toString().trim()
            val coordinatorUserName = binding.coordinatorUserName.text.toString().trim()
            val coordinatorPassword = binding.coordinatorPassword.text.toString().trim()
            val coordinatorConfirmPassword = binding.coordinatorConfirmPassword.text.toString().trim()

            // ✅ Validate fields
            if (coordinatorFirstName.isEmpty() || coordinatorLastName.isEmpty() || coordinatorBirthday.isEmpty() ||
                coordinatorGender.isEmpty() || coordinatorContact.isEmpty() || coordinatorEmail.isEmpty() ||
                coordinatorUserName.isEmpty() || coordinatorPassword.isEmpty() || coordinatorConfirmPassword.isEmpty()
            ) {
                showTopToast("All fields are required")
                return@setOnClickListener
            }

            // ✅ Validate PH number
            if (!coordinatorContact.matches(Regex("^09[0-9]{9}$"))) {
                showTopToast("Contact must be 11 digits and start with 09")
                return@setOnClickListener
            }

            // ✅ Passwords match
            if (coordinatorPassword != coordinatorConfirmPassword) {
                showTopToast("Passwords do not match")
                return@setOnClickListener
            }

            // ✅ Strong password check
            if (!isStrongPassword(coordinatorPassword)) {
                showTopToast("Password must contain uppercase, lowercase, number")
                return@setOnClickListener
            }


            registerCoordinator(
                coordinatorFirstName, coordinatorMiddleName, coordinatorLastName, coordinatorBirthday,
                coordinatorGender, coordinatorContact, coordinatorEmail, coordinatorUserName, coordinatorPassword
            )
        }
    }

    // 🔹 Auto Capitalize Each Word
    private fun setupNameCapitalization(editText: android.widget.EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var isEditing = false
            override fun afterTextChanged(s: Editable?) {
                if (!isEditing && !s.isNullOrEmpty()) {
                    isEditing = true
                    val formatted = formatName(s.toString())
                    editText.setText(formatted)
                    editText.setSelection(formatted.length)
                    isEditing = false
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun formatName(name: String): String {
        return name.lowercase(Locale.getDefault()).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }

    private fun setupLiveValidation() {
        // Username
        binding.coordinatorUserName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val coordinatoruUsername = s.toString().trim()
                if (coordinatoruUsername.isNotEmpty()) checkField("coordinator_username", coordinatoruUsername) { exists ->
                    binding.coordinatorUserName.error = if (exists) "Username already taken" else null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Email
        binding.coordinatorEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val coordinatorEmail = s.toString().trim()
                if (coordinatorEmail.isNotEmpty()) checkField("coordinator_email", coordinatorEmail) { exists ->
                    binding.coordinatorEmail.error = if (exists) "Email already exists" else null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Contact
        binding.coordinatorContact.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val coordinatorContact = s.toString().trim()

                if (coordinatorContact.isNotEmpty() && !coordinatorContact.startsWith("09")) {
                    binding.coordinatorContact.error = "Must start with 09"
                } else if (coordinatorContact.length in 1..10) {
                    binding.coordinatorContact.error = "Must be 11 digits"
                } else if (coordinatorContact.length == 11) {
                    if (!coordinatorContact.matches(Regex("^09[0-9]{9}$"))) {
                        binding.coordinatorContact.error = "Invalid number"
                    } else {
                        checkField("contact", coordinatorContact) { exists ->
                            binding.coordinatorContact.error = if (exists) "Contact already exists" else null
                        }
                    }
                } else {
                    binding.coordinatorContact.error = null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Password strength check
        binding.coordinatorPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val coordinatorPassword = s.toString().trim()
                if (!isStrongPassword(coordinatorPassword)) {
                    binding.coordinatorPassword.error =
                        "Password must contain uppercase, lowercase & number (min 8 chars)"

                } else {
                    binding.coordinatorPassword.error = null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun isStrongPassword(password: String): Boolean {
        // Requires at least 1 uppercase, 1 lowercase, 1 number, and min 8 chars
        val passwordPattern =
            Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$")
        return password.matches(passwordPattern)
    }


    private fun checkField(field: String, value: String, callback: (Boolean) -> Unit) {
        val url = "http://192.168.254.193/sapa_api/check_field.php"
        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                callback(response.contains("exists", ignoreCase = true))
            },
            { _ ->
                callback(false)
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["field"] = field
                params["value"] = value
                return params
            }
        }
        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun registerCoordinator(
        coordinatorFirstName: String,
        coordinatorMiddleName: String,
        coordinatorLastName: String,
        coordinatorBirthday: String,
        coordinatorGender: String,
        coordinatorContact: String,
        coordinatorEmail: String,
        coordinatorUserName: String,
        coordinatorPassword: String
    ) {
        val url = "http://192.168.254.193/sapa_api/register.php"

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                showTopToast(response)

                if (response.contains("Registered Successfully", ignoreCase = true)) {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            },
            { error ->
                showTopToast("Error: ${error.message}")
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["coordinator_first_name"] = coordinatorFirstName
                params["coordinator_middle_name"] = coordinatorMiddleName
                params["coordinator_last_name"] = coordinatorLastName
                params["coordinator_birthday"] = coordinatorBirthday
                params["coordinator_gender"] = coordinatorGender
                params["coordinator_contact"] = coordinatorContact
                params["coordinator_email"] = coordinatorEmail
                params["coordinator_username"] = coordinatorUserName
                params["coordinator_password"] = coordinatorPassword
                params["coordinator_status"] = "Pending"
                return params
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun showTopToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 150)
        toast.show()
    }
}
