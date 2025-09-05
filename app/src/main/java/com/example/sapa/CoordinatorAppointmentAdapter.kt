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
    val hospitalEmail: String,
    val hospitalAddress: String,// Only the hospital name is required// Only the hospital name is required

)

class CoordinatorAppointmentAdapter(
    private val context: Context,
    private val coordinatorHospital: MutableList<CoordinatorHospital>
) : RecyclerView.Adapter<CoordinatorAppointmentAdapter.AppointmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding = ItemCoordinatorHospitalBinding.inflate(LayoutInflater.from(context), parent, false)
        return AppointmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val hospital = coordinatorHospital[position]

        holder.binding.hospitalNumber.text =  hospital.id
        holder.binding.hospitalName.text = hospital.hospitalName
        holder.binding.hospitalAddress.text = hospital.hospitalAddress

        holder.binding.btnSelect.setOnClickListener {
            val intent = Intent(context, CoordinatorAllocationActivity::class.java).apply {
                putExtra("hospital_email", hospital.hospitalEmail)
                putExtra("hospital_name", hospital.hospitalName)
                putExtra("hospital_address", hospital.hospitalAddress)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = coordinatorHospital.size

    fun updateList(newHospitals: List<CoordinatorHospital>) {
        coordinatorHospital.clear()
        coordinatorHospital.addAll(newHospitals)
        notifyDataSetChanged()
    }

    fun removeAppointmentById(id: String) {
        val index = coordinatorHospital.indexOfFirst { it.id == id }
        if (index != -1) {
            coordinatorHospital.removeAt(index)
            notifyItemRemoved(index)
        }
    }


    inner class AppointmentViewHolder(val binding: ItemCoordinatorHospitalBinding) :
        RecyclerView.ViewHolder(binding.root)
}
