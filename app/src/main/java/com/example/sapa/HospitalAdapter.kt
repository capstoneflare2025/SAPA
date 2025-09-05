package com.example.sapa

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sapa.databinding.ItemAllocationBlockBinding

// AllocationBilling data model with hospitalEmail instead of hospitalId
data class AllocationBilling(
    val id: Int,
    val allocationName: String,
    val allocationTimeSlot: String,
    val allocationBillingInfo: String,
    val hospitalEmail: String, // Now using hospitalEmail instead of hospitalId
    val allocationSection: String // Now using hospitalEmail instead of hospitalId
)

class AllocationAdapter(
    private val context: Context,
    private val hospitalEmail: String,  // Passing hospitalEmail
    private val allocations: MutableList<AllocationBilling> // List of AllocationBilling
) : RecyclerView.Adapter<AllocationAdapter.AllocationViewHolder>() {

    // Create a new ViewHolder for the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllocationViewHolder {
        val binding = ItemAllocationBlockBinding.inflate(LayoutInflater.from(context), parent, false)
        return AllocationViewHolder(binding)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: AllocationViewHolder, position: Int) {
        val allocation = allocations[position]

        // Bind allocation daqta to the views
        holder.binding.txtNumber.text = allocation.id.toString() // Display index (1-based)
        holder.binding.allocatioName.text = allocation.allocationName  // Allocation name
        // Handle delete button

        holder.binding.btnDelete.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this allocation?")
                .setPositiveButton("Yes") { dialog, _ ->
                    allocations.removeAt(holder.adapterPosition)
                    notifyItemRemoved(holder.adapterPosition)
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

    }

    // Return the total number of items
    override fun getItemCount(): Int = allocations.size

    // Method to add a new allocation to the list and update the RecyclerView
    fun addAllocation(allocation: AllocationBilling) {
        allocations.add(allocation)  // Add the new allocation
        notifyItemInserted(allocations.size - 1)  // Notify RecyclerView to update
    }

    // ViewHolder class to hold the binding instance
    inner class AllocationViewHolder(val binding: ItemAllocationBlockBinding) :
        RecyclerView.ViewHolder(binding.root)
}

