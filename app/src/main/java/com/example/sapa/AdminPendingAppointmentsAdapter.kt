package com.example.sapa

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sapa.databinding.ActivityAdminAppointmentDetailsBinding
import com.example.sapa.databinding.ItemPendingAppointmentsBinding

class AdminPendingAppointmentsAdapter(
    private val context: Context,
    private val appointments: MutableList<CoordinatorAppointmentList>
) : RecyclerView.Adapter<AdminPendingAppointmentsAdapter.PendingAppointmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingAppointmentViewHolder {
        val binding = ItemPendingAppointmentsBinding.inflate(LayoutInflater.from(context), parent, false)
        return PendingAppointmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PendingAppointmentViewHolder, position: Int) {
        val appointment = appointments[position]

        holder.binding.appointmentNumber.text = appointment.id
        holder.binding.hospitalPendingName.text = appointment.hospitalName
        holder.binding.appointmentStatus.text = appointment.appointmentStatus

        holder.binding.btnDetails.setOnClickListener {
            val intent = Intent(context, AdminAppointmenDetailsActivity::class.java)
            intent.putExtra("appointment_id", appointment.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = appointments.size

    inner class PendingAppointmentViewHolder(val binding: ItemPendingAppointmentsBinding) :
        RecyclerView.ViewHolder(binding.root)
}
