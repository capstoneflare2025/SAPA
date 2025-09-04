package com.example.sapa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class PendingSchool(
    val id: Int,
    val schoolName: String,
    val schoolEmail: String,
    val schoolContact: String,
    val schoolStreet: String,
    val schoolCity: String,
    val schoolProvince: String,
    val schoolAddress: String,
    val coordinatorName: String,
    val coordinatorEmail: String,
    var schoolStatus: String
)

class PendingSchoolAdapter(
    private var schools: MutableList<PendingSchool>
) : RecyclerView.Adapter<PendingSchoolAdapter.PendingSchoolViewHolder>() {

    var onViewDetails: ((PendingSchool) -> Unit)? = null

    inner class PendingSchoolViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNumber: TextView = itemView.findViewById(R.id.txtNumber)
        val txtSchoolName: TextView = itemView.findViewById(R.id.schoolName)
        val txtSchoolStatus: TextView = itemView.findViewById(R.id.schoolStatus)
        val btnViewDetails: ImageView = itemView.findViewById(R.id.btnViewDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingSchoolViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pending_schools, parent, false)
        return PendingSchoolViewHolder(view)
    }

    override fun onBindViewHolder(holder: PendingSchoolViewHolder, position: Int) {
        val school = schools[position]
        holder.txtNumber.text = (position + 1).toString()
        holder.txtSchoolName.text = school.schoolName
        holder.txtSchoolStatus.text = "Status: ${school.schoolStatus}"

        // 🔹 Pass full object to dialog when "view details" is clicked
        holder.btnViewDetails.setOnClickListener {
            onViewDetails?.invoke(school)
        }
    }

    override fun getItemCount(): Int = schools.size

    fun updateData(newSchools: List<PendingSchool>) {
        schools.clear()
        schools.addAll(newSchools)
        notifyDataSetChanged()
    }

    fun removeSchoolById(schoolId: Int) {
        val index = schools.indexOfFirst { it.id == schoolId }
        if (index != -1) {
            schools.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}
