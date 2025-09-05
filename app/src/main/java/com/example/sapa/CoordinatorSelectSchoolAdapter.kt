package com.example.sapa

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class SelectSchool(
    val id: Int,
    val schoolName: String,
    val schoolEmail: String,
    val schoolStreet: String,
    val schoolCity: String,
    val schoolProvince: String,
    val schoolAddress: String
)

class CoordinatorSelectSchoolAdapter(
    private var schoolList: List<SelectSchool>,
    private val hospitalEmail: String?,
    private val hospitalName: String?,
    private val hospitalAddress: String?,
    private val allocationName: String?,
    private val allocationSection: String?,
    private val allocationTimeSlot: String?,
    private val allocationBillingInfo: String?,
    private val onActionClick: (action: String, school: SelectSchool) -> Unit
) : RecyclerView.Adapter<CoordinatorSelectSchoolAdapter.SchoolViewHolder>() {

    class SchoolViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNumber: TextView = itemView.findViewById(R.id.txtNumber)
        val txtName: TextView = itemView.findViewById(R.id.schoolName)
        val txtEmail: TextView = itemView.findViewById(R.id.schoolEmail)
        val txtAddress: TextView = itemView.findViewById(R.id.schoolAddress)
        val btnSelect: ImageView = itemView.findViewById(R.id.btnSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchoolViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_select_school, parent, false)
        return SchoolViewHolder(view)
    }

    override fun onBindViewHolder(holder: SchoolViewHolder, position: Int) {
        val school = schoolList[position]

        holder.txtNumber.text = school.id.toString()
        holder.txtName.text = school.schoolName
        holder.txtEmail.text = school.schoolEmail
        holder.txtAddress.text = "${school.schoolProvince}, ${school.schoolCity}, ${school.schoolStreet}"

        holder.btnSelect.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, CoordinatorSelectStudentActivity::class.java)

            intent.putExtra("school_name", school.schoolName)
            intent.putExtra("school_email", school.schoolEmail)
            intent.putExtra("school_street", school.schoolStreet)
            intent.putExtra("school_city", school.schoolCity)
            intent.putExtra("school_province", school.schoolProvince)
            intent.putExtra("school_address", school.schoolAddress)

            // Pass hospital details
            intent.putExtra("hospital_email", hospitalEmail)
            intent.putExtra("hospital_name", hospitalName)
            intent.putExtra("hospital_address", hospitalAddress)

            // Pass allocation details
            intent.putExtra("allocation_name", allocationName)
            intent.putExtra("allocation_section", allocationSection)
            intent.putExtra("time_slot", allocationTimeSlot)
            intent.putExtra("billing_info", allocationBillingInfo)

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = schoolList.size

    fun updateList(newList: List<SelectSchool>) {
        schoolList = newList
        notifyDataSetChanged()
    }
}
