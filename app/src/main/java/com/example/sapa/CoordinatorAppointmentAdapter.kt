package com.example.sapa

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sapa.databinding.ItemCoordinatorHospitalBinding

// Data model
data class CoordinatorHospital(
    val id: String,
    val hospitalName: String,
    val hospitalEmail: String,// Only the hospital name is required

)

class CoordinatorAppointmentAdapter(
    private val context: Context,
    private val coordinatorHospital: MutableList<CoordinatorHospital> // List of Hospital objects
) : RecyclerView.Adapter<CoordinatorAppointmentAdapter.AppointmentViewHolder>() {

    // Create a new ViewHolder for the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding = ItemCoordinatorHospitalBinding.inflate(LayoutInflater.from(context), parent, false)
        return AppointmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val hospital = coordinatorHospital[position]

        // Bind only the hospital name
        holder.binding.hospitalName.text = hospital.hospitalName // Display the hospital name

        // Set click listener on the select button to pass data to the next activity
        holder.binding.btnSelect.setOnClickListener {
            val intent = Intent(context, CoordinatorAllocationActivity::class.java)
            intent.putExtra("hospital_email", hospital.hospitalEmail)  // Pass the hospital name or email
            context.startActivity(intent)
        }
    }

    // Return the total number of items
    override fun getItemCount(): Int = coordinatorHospital.size

    // Method to update the list of hospitals and notify the adapter to refresh the RecyclerView
    fun updateList(newHospitals: List<CoordinatorHospital>) {
        coordinatorHospital.clear()
        coordinatorHospital.addAll(newHospitals)
        notifyDataSetChanged()  // Ensure the RecyclerView is updated
    }

    // ViewHolder class to hold the binding instance
    inner class AppointmentViewHolder(val binding: ItemCoordinatorHospitalBinding) :
        RecyclerView.ViewHolder(binding.root)
}
