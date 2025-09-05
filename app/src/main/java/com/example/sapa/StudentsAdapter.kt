package com.example.sapa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.Serializable

data class Students(
    val id: Int,
    val studentFirstName: String,
    val studentMiddleName: String,
    val studentLastName: String,
    val schoolName: String
) {
    val studentFullName: String
        get() = "$studentFirstName $studentMiddleName $studentLastName".replace("  ", " ")
}

class StudentsAdapter(
    private var studentsList: List<Students>,
    private val onActionClick: (String, Students) -> Unit
) : RecyclerView.Adapter<StudentsAdapter.StudentsViewHolder>() {

    class StudentsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNumber: TextView = itemView.findViewById(R.id.txtNumber)
        val txtName: TextView = itemView.findViewById(R.id.txtStudentName) // fixed ID
        val btnViewDetails: ImageView = itemView.findViewById(R.id.btnViewDetails)
        val btnBills: ImageView = itemView.findViewById(R.id.btnBills)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentsViewHolder, position: Int) {
        val students = studentsList[position]

        holder.txtNumber.text = students.id.toString()
        holder.txtName.text = students.studentFullName

        holder.btnViewDetails.setOnClickListener { onActionClick("details", students) }
        holder.btnBills.setOnClickListener { onActionClick("bills", students) }
    }


    override fun getItemCount(): Int = studentsList.size

    fun updateList(newList: List<Students>) {
        studentsList = newList
        notifyDataSetChanged()
    }
}

