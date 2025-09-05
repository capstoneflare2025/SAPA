    package com.example.sapa

    import android.content.Intent
    import android.os.Bundle
    import android.util.Log
    import android.widget.Toast
    import androidx.appcompat.app.AlertDialog
    import androidx.appcompat.app.AppCompatActivity
    import androidx.recyclerview.widget.LinearLayoutManager
    import com.example.sapa.databinding.ActivityAdminAddHospitalBillingAllocationBinding
    import okhttp3.*
    import java.io.IOException

    class AdminAddHospitalBillingAllocationActivity : AppCompatActivity() {

        private lateinit var binding: ActivityAdminAddHospitalBillingAllocationBinding
        private val allocationsList = mutableListOf<AllocationBilling>()

        // Variables to hold hospital data
        private var hospitalName: String? = null
        private var hospitalEmail: String? = null
        private var hospitalContact: String? = null
        private var hospitalType: String? = null
        private var hospitalStreet: String? = null
        private var hospitalCity: String? = null
        private var hospitalProvince: String? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityAdminAddHospitalBillingAllocationBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Retrieve hospital data passed from the previous activity
            hospitalName = intent.extras?.getString("hospital_name")
            hospitalEmail = intent.extras?.getString("hospital_email")
            hospitalContact = intent.extras?.getString("hospital_contact")
            hospitalType = intent.extras?.getString("hospital_type")
            hospitalStreet = intent.extras?.getString("hospital_street")
            hospitalCity = intent.extras?.getString("hospital_city")
            hospitalProvince = intent.extras?.getString("hospital_province")

            // Set up RecyclerView
            val allocationAdapter = AllocationAdapter(
                this,                      // Context
                hospitalEmail.orEmpty(),    // Pass hospitalEmail correctly
                allocationsList             // List of allocations
            )
            binding.recylerAllocation.layoutManager = LinearLayoutManager(this)
            binding.recylerAllocation.adapter = allocationAdapter

            // Add new allocation
            binding.btnAddAllocation.setOnClickListener {
                val allocationName = binding.allocationName.text.toString()
                val timeSlot = binding.timeSlot.text.toString()
                val billingInfo = binding.billingInfo.text.toString()
                val allocationSection = binding.allocationSection.text.toString()

                if (allocationName.isNotEmpty() && timeSlot.isNotEmpty() && billingInfo.isNotEmpty()) {
                    val newAllocation = AllocationBilling(
                        id = 0, // To be updated after insertion
                        allocationName = allocationName,
                        allocationSection = allocationSection,// Ensure hospitalEmail is passed
                        allocationTimeSlot = timeSlot,
                        allocationBillingInfo = billingInfo,
                        hospitalEmail = hospitalEmail.orEmpty()
                    )
                    allocationsList.add(newAllocation)
                    allocationAdapter.notifyItemInserted(allocationsList.size - 1)
                    binding.allocationName.text?.clear()
                    binding.allocationSection.text?.clear()
                    binding.timeSlot.text?.clear()
                    binding.billingInfo.text?.clear()
                    Toast.makeText(this, "Allocation added to list", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                }
            }

            binding.btnSave.setOnClickListener {
                if (allocationsList.isNotEmpty()) {
                    AlertDialog.Builder(this)
                        .setTitle("Confirm Save")
                        .setMessage("Are you sure you want to save the allocation data? Once saved, changes cannot be undone.")
                        .setPositiveButton("Yes, Save") { dialog, _ ->
                            dialog.dismiss()
                            saveDataToDatabase(allocationsList)
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                } else {
                    Toast.makeText(this, "No allocations to save", Toast.LENGTH_SHORT).show()
                }
            }



            binding.btnBack.setOnClickListener {
                if (isDataEntered()) {
                    showDiscardChangesDialog()
                } else {
                    finishWithResult()
                }
            }

        }

        override fun onBackPressed() {
            if (isDataEntered()) {
                showDiscardChangesDialog()
            } else {
                super.onBackPressed()
            }
        }

        private fun isDataEntered(): Boolean {
            // Check if any input field has text
            val anyFieldFilled = listOf(
                binding.allocationName.text.toString(),
                binding.allocationSection.text.toString(),
                binding.timeSlot.text.toString(),
                binding.billingInfo.text.toString()
            ).any { it.isNotBlank() }

            // Check if allocations list has items
            val anyAllocationAdded = allocationsList.isNotEmpty()

            return anyFieldFilled || anyAllocationAdded
        }

        private fun showDiscardChangesDialog() {
            AlertDialog.Builder(this)
                .setTitle("Discard Changes?")
                .setMessage("You have unsaved allocation data. Are you sure you want to go back? All entered data will be lost.")
                .setPositiveButton("Yes, discard") { dialog, _ ->
                    dialog.dismiss()
                    finish()  // Close activity and discard changes
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss() // Just dismiss dialog and stay
                }
                .show()
        }



        private fun finishWithResult() {
            val resultIntent = Intent().apply {
                putExtra("hospital_name", hospitalName)
                putExtra("hospital_email", hospitalEmail)
                putExtra("hospital_contact", hospitalContact)
                putExtra("hospital_type", hospitalType)
                putExtra("hospital_street", hospitalStreet)
                putExtra("hospital_city", hospitalCity)
                putExtra("hospital_province", hospitalProvince)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }


        private fun saveDataToDatabase(allocations: List<AllocationBilling>) {
            val client = OkHttpClient()

            // Prepare the hospital data for insertion into the `Hospitals` table using FormBody
            val hospitalFormBody = FormBody.Builder()
                .add("hospital_name", hospitalName.orEmpty())
                .add("hospital_email", hospitalEmail.orEmpty()) // Use email to identify hospital
                .add("hospital_contact", hospitalContact.orEmpty())
                .add("hospital_type", hospitalType.orEmpty())
                .add("hospital_street", hospitalStreet.orEmpty())
                .add("hospital_city", hospitalCity.orEmpty())
                .add("hospital_province", hospitalProvince.orEmpty())
                .build()

            // Log the hospital data being sent
            Log.d("AdminAddHospital", "Hospital Data: $hospitalFormBody")

            // Insert hospital data first
            val hospitalRequest = Request.Builder()
                .url("http://192.168.254.193/sapa_api/add_hospital/add_hospital.php") // Update this URL
                .post(hospitalFormBody)
                .build()

            client.newCall(hospitalRequest).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("AdminAddHospital", "Error: ${e.message}") // Log error on failure
                    runOnUiThread {
                        Toast.makeText(this@AdminAddHospitalBillingAllocationActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        Log.d("AdminAddHospital", "Hospital Insert Response: ${response.body?.string()}")

                        var successCount = 0
                        var failedCount = 0

                        allocations.forEachIndexed { index, allocation ->
                            val allocationFormBody = FormBody.Builder()
                                .add("allocation_name", allocation.allocationName)
                                .add("allocation_section", allocation.allocationSection)
                                .add("time_slot", allocation.allocationTimeSlot)
                                .add("billing_info", allocation.allocationBillingInfo)
                                .add("hospital_email", hospitalEmail.orEmpty())
                                .build()

                            val allocationRequest = Request.Builder()
                                .url("http://192.168.254.193/sapa_api/add_hospital/add_allocation_billing.php")
                                .post(allocationFormBody)
                                .build()

                            client.newCall(allocationRequest).enqueue(object : Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    Log.e("AdminAddHospital", "Error adding allocation: ${e.message}")
                                    failedCount++
                                    checkIfAllCompleted()
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    if (response.isSuccessful) {
                                        successCount++
                                    } else {
                                        failedCount++
                                    }
                                    checkIfAllCompleted()
                                }

                                fun checkIfAllCompleted() {
                                    if (successCount + failedCount == allocations.size) {
                                        runOnUiThread {
                                            Toast.makeText(
                                                this@AdminAddHospitalBillingAllocationActivity,
                                                "Saved $successCount out of ${allocations.size} allocations",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            navigateBackToAdminHospitalFragment()
                                        }
                                    }
                                }
                            })
                        }

                        // ❌ Remove this block
                        // runOnUiThread {
                        //     Toast.makeText(this@AdminAddHospitalBillingAllocationActivity, "Hospital and allocation data saved successfully", Toast.LENGTH_SHORT).show()
                        //     navigateBackToFragment()
                        // }

                    } else {
                        Log.e("AdminAddHospital", "Failed to add hospital")
                        runOnUiThread {
                            Toast.makeText(this@AdminAddHospitalBillingAllocationActivity, "Failed to add hospital", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            })
        }

        private fun navigateBackToAdminHospitalFragment() {
            val intent = Intent(this, AdminDashboardActivity::class.java).apply {
                putExtra("show_fragment", "AdminHospitalFragment")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }


    }
