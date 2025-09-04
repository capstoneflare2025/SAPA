package com.example.sapa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Data class
data class School(
    val id: Int,
    val schoolName: String,
    val schoolStatus: String,
    val schoolEmail: String,
    val schoolContact: String,
    val schoolStreet: String,
    val schoolCity: String,
    val schoolProvince: String,
    val schoolAddress: String,
)


class SchoolAdapter(
    private var schoolList: List<School>,
    private val onActionClick: (action: String, school: School) -> Unit
) : RecyclerView.Adapter<SchoolAdapter.SchoolViewHolder>() {

    class SchoolViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNumber: TextView = itemView.findViewById(R.id.txtNumber)
        val txtName: TextView = itemView.findViewById(R.id.schoolName)
        val btnViewDetails: ImageView = itemView.findViewById(R.id.btnViewDetails)
        val btnListStudents: ImageView = itemView.findViewById(R.id.btnListStudents)
        val btnBills: ImageView = itemView.findViewById(R.id.btnBills)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchoolViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_school, parent, false)
        return SchoolViewHolder(view)
    }

    override fun onBindViewHolder(holder: SchoolViewHolder, position: Int) {
        val school = schoolList[position]

        holder.txtNumber.text = (position + 1).toString()
        holder.txtName.text = "${school.schoolName}" // 👈 show status beside name

        holder.btnViewDetails.setOnClickListener { onActionClick("details", school) }
        holder.btnListStudents.setOnClickListener { onActionClick("students", school) }
        holder.btnBills.setOnClickListener { onActionClick("bills", school) }
    }

    override fun getItemCount(): Int = schoolList.size

    fun updateList(newList: List<School>) {
        schoolList = newList
        notifyDataSetChanged()
    }
}

