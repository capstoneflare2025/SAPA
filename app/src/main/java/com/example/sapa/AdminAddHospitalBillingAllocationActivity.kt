package com.example.sapa

import android.os.Bundle
import android.util.Log
import android.widget.Toast
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

        // Handle save all button click to insert data
        binding.btnSave.setOnClickListener {
            if (allocationsList.isNotEmpty()) {
                saveDataToDatabase(allocationsList)
            } else {
                Toast.makeText(this, "No allocations to save", Toast.LENGTH_SHORT).show()
            }
        }
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
                    // Log the server response for hospital insertion
                    Log.d("AdminAddHospital", "Hospital Insert Response: ${response.body?.string()}")

                    // After hospital insertion, insert allocation data using hospital_email
                    allocations.forEach { allocation ->
                        val allocationFormBody = FormBody.Builder()
                            .add("allocation_name", allocation.allocationName)
                            .add("allocation_section", allocation.allocationSection) // Use email to link allocations
                            .add("time_slot", allocation.allocationTimeSlot)
                            .add("billing_info", allocation.allocationBillingInfo)
                            .add("hospital_email", hospitalEmail.orEmpty()) // Use email to link allocations
                            .build()

                        // Log the allocation data being sent
                        Log.d("AdminAddHospital", "Allocation Data: $allocationFormBody")

                        val allocationRequest = Request.Builder()
                            .url("http://192.168.254.193/sapa_api/add_hospital/add_allocation_billing.php") // Update this URL
                            .post(allocationFormBody)
                            .build()

                        client.newCall(allocationRequest).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                Log.e("AdminAddHospital", "Error adding allocation: ${e.message}")
                                runOnUiThread {
                                    Toast.makeText(this@AdminAddHospitalBillingAllocationActivity, "Error adding allocation: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onResponse(call: Call, response: Response) {
                                if (response.isSuccessful) {
                                    Log.d("AdminAddHospital", "Allocation Insert Response: ${response.body?.string()}")
                                    runOnUiThread {
                                        Toast.makeText(this@AdminAddHospitalBillingAllocationActivity, "Allocation added successfully", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Log.e("AdminAddHospital", "Failed to add allocation")
                                    runOnUiThread {
                                        Toast.makeText(this@AdminAddHospitalBillingAllocationActivity, "Failed to add allocation", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        })
                    }

                    // After successful save, navigate to the correct place
                    runOnUiThread {
                        Toast.makeText(this@AdminAddHospitalBillingAllocationActivity, "Hospital and allocation data saved successfully", Toast.LENGTH_SHORT).show()
                        navigateBackToFragment()  // Assuming you want to return to the AdminHospitalFragment
                    }
                } else {
                    Log.e("AdminAddHospital", "Failed to add hospital")
                    runOnUiThread {
                        Toast.makeText(this@AdminAddHospitalBillingAllocationActivity, "Failed to add hospital", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun navigateBackToFragment() {
        // Only replace the fragment if it's not already in the back stack
        val fragment = AdminHospitalFragment()
        val transaction = supportFragmentManager.beginTransaction()

        val fragmentTag = AdminHospitalFragment::class.java.simpleName
        if (supportFragmentManager.findFragmentByTag(fragmentTag) == null) {
            transaction.replace(R.id.fragment_container_hospital, fragment, fragmentTag)
            transaction.addToBackStack(null)  // Add to back stack so that the user can navigate back
            transaction.commit()  // Commit immediately (avoid commitNow(), as it can cause timing issues)
        } else {
            Log.d("AdminAddHospital", "Fragment already in back stack, not adding again.")
        }

        // Finish the activity to remove it from the stack
        finish()  // This will close the current activity and navigate to the fragment view
    }

}
