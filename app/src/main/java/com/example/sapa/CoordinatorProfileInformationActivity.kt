package com.example.sapa

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sapa.databinding.ActivityCoordinatorProfileInformationBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class CoordinatorProfileInformationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCoordinatorProfileInformationBinding
    private val client = OkHttpClient()

    private var savedGender: String? = null
    private var isEditingPersonal = false
    private var isEditingAccount = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoordinatorProfileInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBirthdayPicker()
        setupContactValidation()
        setupPasswordLogic()
        fetchUserProfile()

        binding.btnUpdatePersonal.setOnClickListener {
            if (!isEditingPersonal) {
                enablePersonalFields(true)
                binding.btnUpdatePersonal.text = "Save"
                isEditingPersonal = true
            } else {
                updatePersonalInfo()
                enablePersonalFields(false)
                binding.btnUpdatePersonal.text = "Update Personal Info"
                isEditingPersonal = false
            }
        }

        binding.btnUpdateAccount.setOnClickListener {
            if (!isEditingAccount) {
                enableAccountFields(true)
                binding.btnUpdateAccount.text = "Save"
                isEditingAccount = true
            } else {
                validateAndUpdateAccount()
                enableAccountFields(false)
                binding.btnUpdateAccount.text = "Update Account Info"
                isEditingAccount = false
            }
        }
    }

    private fun fetchUserProfile() {
        val sharedPref = getSharedPreferences("SAPA_PREFS", MODE_PRIVATE)
        val sessionEmail = sharedPref.getString("coordinator_email", null)

        if (sessionEmail.isNullOrEmpty()) {
            Toast.makeText(this, "No session found", Toast.LENGTH_SHORT).show()
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
                runOnUiThread {
                    Toast.makeText(this@CoordinatorProfileInformationActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body?.string()
                val json = JSONObject(res ?: "{}")

                runOnUiThread {
                    binding.coordinatorFirstName.setText(json.optString("coordinator_first_name"))
                    binding.coordinatorMiddleName.setText(json.optString("coordinator_middle_name"))
                    binding.coordinatorLastName.setText(json.optString("coordinator_last_name"))
                    binding.coordinatorBirthday.setText(formatBirthday(json.optString("coordinator_birthday")))
                    binding.coordinatorContact.setText(json.optString("coordinator_contact"))
                    savedGender = json.optString("coordinator_gender")
                    binding.coordinatorUserName.setText(json.optString("coordinator_username"))
                    binding.coordinatorEmail.setText(json.optString("coordinator_email"))

                    setupGenderDropdown(savedGender)
                }
            }
        })
    }

    private fun setupGenderDropdown(existingGender: String? = null) {
        val genders = listOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genders)
        binding.coordinatorSpinnerGender.setAdapter(adapter)

        if (!existingGender.isNullOrEmpty()) {
            if (existingGender == "Male" || existingGender == "Female") {
                binding.coordinatorSpinnerGender.setText(existingGender, false)
                binding.layoutOtherGender.visibility = android.view.View.GONE
            } else {
                binding.coordinatorSpinnerGender.setText("Other", false)
                binding.layoutOtherGender.visibility = android.view.View.VISIBLE
                binding.coordinatorOtherGender.setText(existingGender)
            }
        }

        binding.coordinatorSpinnerGender.setOnItemClickListener { _, _, position, _ ->
            val selected = genders[position]
            if (selected == "Other") {
                binding.layoutOtherGender.visibility = android.view.View.VISIBLE
                binding.coordinatorOtherGender.setText("")
            } else {
                binding.layoutOtherGender.visibility = android.view.View.GONE
                binding.coordinatorOtherGender.setText("")
            }
        }
    }

    private fun setupBirthdayPicker() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yy/MM/dd", Locale.US)
        binding.coordinatorBirthday.inputType = InputType.TYPE_NULL
        binding.coordinatorBirthday.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    binding.coordinatorBirthday.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun formatBirthday(raw: String?): String {
        if (raw.isNullOrEmpty() || raw == "0000-00-00") return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val outputFormat = SimpleDateFormat("yy/MM/dd", Locale.US)
            val date = inputFormat.parse(raw)
            outputFormat.format(date!!)
        } catch (e: ParseException) {
            raw ?: ""
        }
    }

    private fun setupContactValidation() {
        binding.coordinatorContact.inputType = InputType.TYPE_CLASS_NUMBER
        binding.coordinatorContact.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(11))
        binding.coordinatorContact.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                when {
                    input.isEmpty() -> binding.coordinatorContact.error = null
                    !input.startsWith("09") -> binding.coordinatorContact.error = "Must start with 09"
                    input.length < 11 -> binding.coordinatorContact.error = "Must be 11 digits"
                    input.length == 11 && input.startsWith("09") -> binding.coordinatorContact.error = null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupPasswordLogic() {
        binding.layoutNewPassword.visibility = android.view.View.GONE
        binding.layoutConfirmPassword.visibility = android.view.View.GONE
        binding.coordinatorCurrentPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    binding.layoutNewPassword.visibility = android.view.View.VISIBLE
                    binding.layoutConfirmPassword.visibility = android.view.View.VISIBLE
                } else {
                    binding.layoutNewPassword.visibility = android.view.View.GONE
                    binding.layoutConfirmPassword.visibility = android.view.View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun updatePersonalInfo() {
        val sharedPref = getSharedPreferences("SAPA_PREFS", MODE_PRIVATE)
        val sessionEmail = sharedPref.getString("coordinator_email", null) ?: return

        val coordiantorFirstName = binding.coordinatorFirstName.text.toString().trim()
        val coordinatorMiddleName = binding.coordinatorMiddleName.text.toString().trim()
        val coordinatorLastName = binding.coordinatorLastName.text.toString().trim()
        val coordinatorBirthday = binding.coordinatorBirthday.text.toString().trim()
        val coordinatorContact = binding.coordinatorContact.text.toString().trim()
        val coordinatorGender = if (binding.coordinatorSpinnerGender.text.toString() == "Other") {
            binding.coordinatorOtherGender.text.toString().trim()
        } else {
            binding.coordinatorSpinnerGender.text.toString()
        }

        val builder = FormBody.Builder()
        builder.add("coordinator_email", sessionEmail)

        if (coordiantorFirstName.isNotEmpty()) builder.add("coordinator_first_name", coordiantorFirstName)
        if (coordinatorMiddleName.isNotEmpty()) builder.add("coordinator_middle_name", coordinatorMiddleName)
        if (coordinatorLastName.isNotEmpty()) builder.add("coordinator_last_name", coordinatorLastName)
        if (coordinatorBirthday.isNotEmpty()) builder.add("coordinator_birthday", coordinatorBirthday)
        if (coordinatorContact.isNotEmpty()) builder.add("coordinator_contact", coordinatorContact)
        if (coordinatorGender.isNotEmpty()) builder.add("coordinator_gender", coordinatorGender)

        val formBody = builder.build()

        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/add_coordinator/update_personal.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CoordinatorProfileInformationActivity, "Failed to update personal info", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    Toast.makeText(this@CoordinatorProfileInformationActivity, "Personal info updated", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun validateAndUpdateAccount() {
        val sharedPref = getSharedPreferences("SAPA_PREFS", MODE_PRIVATE)
        val sessionEmail = sharedPref.getString("coordinator_email", null) ?: return

        val username = binding.coordinatorUserName.text.toString().trim()
        val email = binding.coordinatorEmail.text.toString().trim()
        val currentPass = binding.coordinatorCurrentPassword.text.toString().trim()
        val newPass = binding.coordinatorNewPassword.text.toString().trim()
        val confirmPass = binding.coordinatorConfirmPassword.text.toString().trim()

        if (newPass.isNotEmpty() && newPass != confirmPass) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = FormBody.Builder()
        builder.add("coordinator_email", sessionEmail)

        if (username.isNotEmpty()) builder.add("coordinator_username", username)
        if (email.isNotEmpty()) builder.add("coordinator_new_email", email)
        if (currentPass.isNotEmpty()) builder.add("coordinator_current_password", currentPass)
        if (newPass.isNotEmpty()) builder.add("coordinator_new_password", newPass)

        val formBody = builder.build()

        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/add_coordinator/update_account.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CoordinatorProfileInformationActivity, "Failed to update account", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body?.string()
                val json = JSONObject(res ?: "{}")
                runOnUiThread {
                    val message = json.optString("message", "Account update completed")
                    Toast.makeText(this@CoordinatorProfileInformationActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun enablePersonalFields(enable: Boolean) {
        binding.coordinatorFirstName.isEnabled = enable
        binding.coordinatorMiddleName.isEnabled = enable
        binding.coordinatorLastName.isEnabled = enable
        binding.coordinatorBirthday.isEnabled = enable
        binding.coordinatorSpinnerGender.isEnabled = enable
        binding.coordinatorOtherGender.isEnabled = enable
        binding.coordinatorContact.isEnabled = enable
    }

    private fun enableAccountFields(enable: Boolean) {
        binding.coordinatorEmail.isEnabled = enable
        binding.coordinatorUserName.isEnabled = enable
        binding.coordinatorCurrentPassword.isEnabled = enable
        binding.coordinatorNewPassword.isEnabled = enable
        binding.coordinatorConfirmPassword.isEnabled = enable
    }
}
