package com.example.sapa

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sapa.databinding.ItemCoordinatorAllocationBinding

// Data model
data class CoordinatorHospitalAllocation(
    val id: String,
    val allocationName: String,
    val allocationTimeSlot: String,
    val allocationBillingInfo: String,
    val allocationSection: String
)

class CoordinatorAllocationAdapter(
    private val context: Context,
    private val coordinatorHospitalAllocation: MutableList<CoordinatorHospitalAllocation>,
    private val hospitalEmail: String,
    private val hospitalName: String,
    private val hospitalAddress: String
) : RecyclerView.Adapter<CoordinatorAllocationAdapter.HospitalAllocationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalAllocationViewHolder {
        val binding = ItemCoordinatorAllocationBinding.inflate(LayoutInflater.from(context), parent, false)
        return HospitalAllocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HospitalAllocationViewHolder, position: Int) {
        val allocation = coordinatorHospitalAllocation[position]

        holder.binding.allocationNumber.text = allocation.id
        holder.binding.allocatioName.text = allocation.allocationName
        holder.binding.allocationSection.text = allocation.allocationSection
        holder.binding.allocationTimeSlot.text = allocation.allocationTimeSlot
        holder.binding.allocationBillingInfo.text = allocation.allocationBillingInfo

        holder.binding.btnSelect.setOnClickListener {
            val intent = Intent(context, CoordinatorSelectSchoolActivity::class.java).apply {
                putExtra("hospital_email", hospitalEmail)
                putExtra("hospital_name", hospitalName)
                putExtra("hospital_address", hospitalAddress)

                // Optionally pass allocation details
                putExtra("allocation_name", allocation.allocationName)
                putExtra("allocation_section", allocation.allocationSection)
                putExtra("time_slot", allocation.allocationTimeSlot)
                putExtra("billing_info", allocation.allocationBillingInfo)
            }
            context.startActivity(intent)
        }



    }

    override fun getItemCount(): Int = coordinatorHospitalAllocation.size

    fun updateList(newHospitalAllocations: List<CoordinatorHospitalAllocation>) {
        coordinatorHospitalAllocation.clear()
        coordinatorHospitalAllocation.addAll(newHospitalAllocations)
        notifyDataSetChanged()
    }

    inner class HospitalAllocationViewHolder(val binding: ItemCoordinatorAllocationBinding) :
        RecyclerView.ViewHolder(binding.root)
}
