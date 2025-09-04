package com.example.sapa

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sapa.databinding.ItemAdminHospitalBinding

data class AdminHospital(
    val id: Int,
    val hospitalName: String,
)

class AdminHospitalAdapter(
    private val context: Context,
    private val adminHospitalEmail: String,  // Passing hospitalEmail
    private val adminHospitals: MutableList<AdminHospital> // List of Hospital
) : RecyclerView.Adapter<AdminHospitalAdapter.HospitalViewHolder>() {

    // Create a new ViewHolder for the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalViewHolder {
        val binding = ItemAdminHospitalBinding.inflate(LayoutInflater.from(context), parent, false)
        return HospitalViewHolder(binding)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: HospitalViewHolder, position: Int) {
        val hospital = adminHospitals[position]

        // Bind hospital data to the views
        holder.binding.txtNumber.text = (position + 1).toString()  // Display index (1-based)
        holder.binding.hospitalName.text = hospital.hospitalName  // Hospital name
    }

    // Return the total number of items
    override fun getItemCount(): Int = adminHospitals.size

    // Method to add a new hospital to the list and update the RecyclerView
    fun addHospital(hospital: AdminHospital) {
        adminHospitals.add(hospital)  // Add the new hospital
        notifyItemInserted(adminHospitals.size - 1)  // Notify RecyclerView to update
    }

    // Method to update the entire list and notify RecyclerView
    fun updateData(newHospitals: List<AdminHospital>) {
        adminHospitals.clear()  // Clear the current list
        adminHospitals.addAll(newHospitals)  // Add all new hospitals
        notifyDataSetChanged()  // Notify RecyclerView to refresh
    }

    // ViewHolder class to hold the binding instance
    inner class HospitalViewHolder(val binding: ItemAdminHospitalBinding) :
        RecyclerView.ViewHolder(binding.root)
}


