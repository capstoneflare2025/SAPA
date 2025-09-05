package com.example.sapa

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sapa.databinding.ItemCoordinatorAppointmentListBinding
import kotlin.jvm.java

// Data model
data class CoordinatorAppointmentList(
    val id: String,
    val hospitalName: String,
    val appointmentStatus: String,

)
class CoordinatorAppointmentListAdapter(
    private val context: Context,
    private val coordinatorHospital: MutableList<CoordinatorAppointmentList>
) : RecyclerView.Adapter<CoordinatorAppointmentListAdapter.AppointmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding = ItemCoordinatorAppointmentListBinding.inflate(LayoutInflater.from(context), parent, false)
        return AppointmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val hospital = coordinatorHospital[position]

        holder.binding.appointmentNumber.text = hospital.id
        holder.binding.hospitalName.text = hospital.hospitalName
        holder.binding.appointmentStatus.text = hospital.appointmentStatus

        holder.binding.btnDetails.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, CoordinatorAppoinmentDetailsActivity::class.java)
            intent.putExtra("appointment_id", hospital.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = coordinatorHospital.size

    fun updateList(newHospitals: List<CoordinatorAppointmentList>) {
        coordinatorHospital.clear()
        coordinatorHospital.addAll(newHospitals)
        notifyDataSetChanged()
    }

    inner class AppointmentViewHolder(val binding: ItemCoordinatorAppointmentListBinding) :
        RecyclerView.ViewHolder(binding.root)
}