package com.example.sapa

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sapa.databinding.FragmentCoordinatorProfileBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CoordinatorProfileFragment : Fragment() {

    private var _binding: FragmentCoordinatorProfileBinding? = null
    private val binding get() = _binding!!
    private val client = OkHttpClient()

    private var savedGender: String? = null
    private var isEditingPersonal = false
    private var isEditingAccount = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoordinatorProfileBinding.inflate(inflater, container, false)

        setupBirthdayPicker()
        setupContactValidation()
        setupPasswordLogic()
        fetchUserProfile()

        // Personal Info button
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

        // Account Info button
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

        binding.btnLogout.setOnClickListener {
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

            // Back to login
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
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
            .url("http://192.168.254.193/sapa_api/get_profile.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    _binding?.let {
                        Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body?.string()
                val json = JSONObject(res ?: "{}")

                requireActivity().runOnUiThread {
                    _binding?.let { binding ->
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
            }
        })
    }

    private fun setupGenderDropdown(existingGender: String? = null) {
        val genders = mutableListOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genders)
        binding.coordinatorSpinnerGender.setAdapter(adapter)

        if (!existingGender.isNullOrEmpty()) {
            if (existingGender == "Male" || existingGender == "Female") {
                binding.coordinatorSpinnerGender.setText(existingGender, false)
                binding.layoutOtherGender.visibility = View.GONE
            } else {
                binding.coordinatorSpinnerGender.setText("Other", false)
                binding.layoutOtherGender.visibility = View.VISIBLE
                binding.coordinatorOtherGender.setText(existingGender)
            }
        }

        binding.coordinatorSpinnerGender.setOnItemClickListener { _, _, position, _ ->
            val selected = genders[position]
            if (selected == "Other") {
                binding.layoutOtherGender.visibility = View.VISIBLE
                binding.coordinatorOtherGender.setText("")
            } else {
                binding.layoutOtherGender.visibility = View.GONE
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
                requireContext(),
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
            raw
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
        binding.layoutNewPassword.visibility = View.GONE
        binding.layoutConfirmPassword.visibility = View.GONE
        binding.coordinatorCurrentPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    binding.layoutNewPassword.visibility = View.VISIBLE
                    binding.layoutConfirmPassword.visibility = View.VISIBLE
                } else {
                    binding.layoutNewPassword.visibility = View.GONE
                    binding.layoutConfirmPassword.visibility = View.GONE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun updatePersonalInfo() {
        val sharedPref = requireActivity().getSharedPreferences("SAPA_PREFS", android.content.Context.MODE_PRIVATE)
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
            .url("http://192.168.254.193/sapa_api/update_personal.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    _binding?.let {
                        Toast.makeText(requireContext(), "Failed to update personal info", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onResponse(call: Call, response: Response) {
                requireActivity().runOnUiThread {
                    _binding?.let {
                        Toast.makeText(requireContext(), "Personal info updated", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun validateAndUpdateAccount() {
        val sharedPref = requireActivity().getSharedPreferences("SAPA_PREFS", android.content.Context.MODE_PRIVATE)
        val sessionEmail = sharedPref.getString("coordinator_email", null) ?: return

        val username = binding.coordinatorUserName.text.toString().trim()
        val email = binding.coordinatorEmail.text.toString().trim()
        val currentPass = binding.coordinatorCurrentPassword.text.toString().trim()
        val newPass = binding.coordinatorNewPassword.text.toString().trim()
        val confirmPass = binding.coordinatorConfirmPassword.text.toString().trim()

        if (newPass.isNotEmpty() && newPass != confirmPass) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = FormBody.Builder()
        builder.add("coordinator_email", sessionEmail)

        if (username.isNotEmpty()) builder.add("coordinator_username", username)
        if (email.isNotEmpty()) builder.add("coordinator_new_email", email) // prevent collision with session email
        if (currentPass.isNotEmpty()) builder.add("coordinator_current_password", currentPass)
        if (newPass.isNotEmpty()) builder.add("coordinator_new_password", newPass)

        val formBody = builder.build()

        val request = Request.Builder()
            .url("http://192.168.254.193/sapa_api/update_account.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    _binding?.let {
                        Toast.makeText(requireContext(), "Failed to update account", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body?.string()
                val json = JSONObject(res ?: "{}")
                requireActivity().runOnUiThread {
                    _binding?.let {
                        val message = json.optString("message", "Account update completed")
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
