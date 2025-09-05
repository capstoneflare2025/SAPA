    package com.example.sapa

    import android.content.Intent
    import android.os.Bundle
    import android.view.Gravity
    import android.view.View
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import com.example.sapa.databinding.ActivityLoginBinding
    import org.json.JSONObject
    import java.net.HttpURLConnection
    import java.net.URL

    class LoginActivity : AppCompatActivity() {

        private lateinit var binding: ActivityLoginBinding

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // ✅ Check if user already logged in using saved email
            val sharedPref = getSharedPreferences("SAPA_PREFS", MODE_PRIVATE)
            val savedCoordinatorEmail = sharedPref.getString("coordinator_email", null)
            val savedAdminEmail = sharedPref.getString("admin_email", null)

            if (!savedCoordinatorEmail.isNullOrEmpty()) {
                // User already logged in, go to Coordinator Dashboard
                val intent = Intent(this, CoordinatorDashboardActivity::class.java)
                startActivity(intent)
                finish()
                return
            }else if(!savedAdminEmail.isNullOrEmpty()){
                val intent = Intent(this, AdminDashboardActivity::class.java)
                startActivity(intent)
                finish()
                return
            }

            // Redirect to Register
            binding.tvRegister.setOnClickListener {
                startActivity(Intent(this, RegisterActivity::class.java))
                finish()
            }

            // Login Button
            binding.btnLogin.setOnClickListener {
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()

                if (email.isEmpty() || password.isEmpty()) {
                    showTopToast("Please enter email and password")
                    return@setOnClickListener
                }

                // Show spinner
                binding.progressBar.visibility = View.VISIBLE

                // ✅ Hardcoded Admin
                // ✅ Hardcoded Admin
                if (email.equals("tamayo123@gmail.com", ignoreCase = true)) {
                    // ✅ Show loading message


                    // Save Admin session in SharedPreferences
                    val sharedPref = getSharedPreferences("SAPA_PREFS", MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("admin_email", email)   // ✅ Store admin session
                        apply()
                    }

                    showTopToast("Please wait, logging in as Admin...")
                    binding.progressBar.visibility = View.VISIBLE

                    // ✅ Delay 3 seconds before navigating
                    binding.root.postDelayed({
                        binding.progressBar.visibility = View.GONE
                        startActivity(Intent(this, AdminDashboardActivity::class.java))
                        finish()
                    }, 3000)

                    startActivity(Intent(this, AdminDashboardActivity::class.java))
                    finish()
                }
                else {
                    Thread {
                        try {
                            val url = URL("http://192.168.254.193/sapa_api/add_coordinator/login.php")
                            val postData = "coordinator_email=$email&coordinator_password=$password"

                            val conn = url.openConnection() as HttpURLConnection
                            conn.requestMethod = "POST"
                            conn.doOutput = true
                            conn.outputStream.write(postData.toByteArray())

                            val response = conn.inputStream.bufferedReader().readText().trim()

                            // Simulate spinner delay
                            Thread.sleep(1200)

                            runOnUiThread {
                                binding.progressBar.visibility = View.GONE

                                try {
                                    val json = JSONObject(response)
                                    val status = json.getString("status")   // ✅ match PHP response


                                    when (status.lowercase()) {
                                        "approved" -> {


                                            val data = json.getJSONObject("data")

                                            val id = data.optString("id", "")
                                            val coordinatorFirstName = data.optString("coordinator_first_name", "")
                                            val coordinatorMiddleName = data.optString("coordinator_middle_name", "")
                                            val coordinatorLastName = data.optString("coordinator_last_name", "")
                                            val coordinatorBirthday = data.optString("coordinator_birthday", "")
                                            val coordinatorGender = data.optString("coordinator_gender", "")
                                            val coordinatorContact = data.optString("coordinator_contact", "")
                                            val coordinatorEmail = data.optString("coordinator_email", "")
                                            val coordinatorUserName = data.optString("coordinator_username", "")

    // Save to SharedPreferences
                                            val sharedPref = getSharedPreferences("SAPA_PREFS", MODE_PRIVATE)
                                            with(sharedPref.edit()) {
                                                putString("coordinator_id", id)   // ✅ Save real user_id
                                                putString("coordinator_first_name", coordinatorFirstName)
                                                putString("coordinator_middle_name", coordinatorMiddleName)
                                                putString("coordinator_last_name", coordinatorLastName)
                                                putString("coordinator_birthday", coordinatorBirthday)
                                                putString("coordinator_gender", coordinatorGender)
                                                putString("coordinator_contact", coordinatorContact)
                                                putString("coordinator_email", coordinatorEmail)
                                                putString("coordinator_username", coordinatorUserName)
                                                putString("coordinator_status", status)
                                                apply()
                                            }

// ✅ Show loading message
                                            showTopToast("Please wait, logging in as Coordinator...")
                                            binding.progressBar.visibility = View.VISIBLE

                                            // ✅ Delay 3 seconds before navigating
                                            binding.root.postDelayed({
                                                binding.progressBar.visibility = View.GONE
                                                startActivity(Intent(this, CoordinatorDashboardActivity::class.java))
                                                finish()
                                            }, 3000)

                                            // Go to Coordinator Dashboard
                                            val intent = Intent(this, CoordinatorDashboardActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                        "pending" -> showTopToast("Your account is still pending approval.")
                                        "declined" -> showTopToast("Your account was declined by the admin.")
                                        "invalid_password" -> showTopToast("Invalid password.")
                                        "not_found" -> showTopToast("User not found.")
                                        else -> showTopToast("Unexpected status: $status")
                                    }

                                } catch (e: Exception) {
                                    // Fallback in case server sends plain text
                                    when {
                                        response.contains("not approved", ignoreCase = true) ->
                                            showTopToast("Your account is not approved by admin.")
                                        response.contains("Invalid password", ignoreCase = true) ->
                                            showTopToast("Invalid password.")
                                        response.contains("User not found", ignoreCase = true) ->
                                            showTopToast("User not found.")
                                        else -> showTopToast("Unexpected response: $response")
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
                                binding.progressBar.visibility = View.GONE
                                showTopToast("Error: ${e.message}")
                            }
                        }
                    }.start()
                }
            }
        }

        // ✅ Toast at top
        private fun showTopToast(message: String) {
            val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 200)
            toast.show()
        }
    }
