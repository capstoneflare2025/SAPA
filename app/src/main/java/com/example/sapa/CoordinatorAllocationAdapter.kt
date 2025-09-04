package com.example.sapa

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sapa.databinding.ItemCoordinatorAllocationBinding
import com.example.sapa.databinding.ItemCoordinatorHospitalBinding

// Data model
data class CoordinatorHospitalAllocation(
    val id: String,
    val allocationName: String,
    val allocationTimeSlot: String,
    val allocationBillingInfo: String,// Only the hospital name is required
    val allocationSection: String// Now using hospitalEmail instead of hospitalId

)

class CoordinatorAllocationAdapter(
    private val context: Context,
    private val coordinatorHospitalAllocation: MutableList<CoordinatorHospitalAllocation> // List of Hospital objects
) : RecyclerView.Adapter<CoordinatorAllocationAdapter.HospitalAllocationViewHolder>() {

    // Create a new ViewHolder for the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalAllocationViewHolder {
        val binding = ItemCoordinatorAllocationBinding.inflate(LayoutInflater.from(context), parent, false)
        return HospitalAllocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HospitalAllocationViewHolder, position: Int) {
        val coordianatorHospitalAllocation = coordinatorHospitalAllocation[position]

        // Bind only the hospital name
        holder.binding.allocatioName.text = coordianatorHospitalAllocation.allocationName // Display the hospital name
        holder.binding.allocationSection.text = coordianatorHospitalAllocation.allocationSection // Display the hospital name
        holder.binding.allocationTimeSlot.text = coordianatorHospitalAllocation.allocationTimeSlot // Display the hospital name
        holder.binding.allocationBillingInfo.text = coordianatorHospitalAllocation.allocationBillingInfo // Display the hospital name
    }

    // Return the total number of items
    override fun getItemCount(): Int = coordinatorHospitalAllocation.size

    // Method to update the list of hospitals and notify the adapter to refresh the RecyclerView
    fun updateList(newHospitalAllocations: List<CoordinatorHospitalAllocation>) {
        coordinatorHospitalAllocation.clear()
        coordinatorHospitalAllocation.addAll(newHospitalAllocations)
        notifyDataSetChanged()  // Ensure the RecyclerView is updated
    }

    // ViewHolder class to hold the binding instance
    inner class HospitalAllocationViewHolder(val binding: ItemCoordinatorAllocationBinding) :
        RecyclerView.ViewHolder(binding.root)
}
