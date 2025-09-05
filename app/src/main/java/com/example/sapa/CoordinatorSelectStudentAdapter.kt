package com.example.sapa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class SelectStudents(
    val id: Int,
    val studentFirstName: String,
    val studentMiddleName: String,
    val studentLastName: String,
    val studentEmail: String,
    val schoolName: String
) {
    val studentFullName: String
        get() = listOf(studentFirstName, studentMiddleName, studentLastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
}

class CoordinatorSelectStudentAdapter(
    private var studentsList: List<SelectStudents>,
    private val onActionClick: (String, SelectStudents) -> Unit
) : RecyclerView.Adapter<CoordinatorSelectStudentAdapter.StudentsViewHolder>() {

    private val selectedStudents = mutableSetOf<SelectStudents>()

    class StudentsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNumber: TextView = itemView.findViewById(R.id.txtNumber)
        val txtName: TextView = itemView.findViewById(R.id.txtStudentName)
        val txtEmail: TextView = itemView.findViewById(R.id.txtStudentEmail)
        val checkbox: CheckBox = itemView.findViewById(R.id.checkboxSelectStudent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_select_student, parent, false)
        return StudentsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentsViewHolder, position: Int) {
        val student = studentsList[position]

        holder.txtNumber.text = student.id.toString()
        holder.txtName.text = student.studentFullName
        holder.txtEmail.text = student.studentEmail

        // Remove previous listener to avoid flicker issues
        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = selectedStudents.contains(student)

        // Checkbox listener to track selection
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedStudents.add(student) else selectedStudents.remove(student)
        }

        // Clicking anywhere on the item toggles checkbox
        holder.itemView.setOnClickListener {
            holder.checkbox.isChecked = !holder.checkbox.isChecked
        }
    }

    override fun getItemCount(): Int = studentsList.size

    fun updateList(newList: List<SelectStudents>) {
        studentsList = newList
        selectedStudents.clear() // Clear selections on new list load
        notifyDataSetChanged()
    }

    // Returns selected students list
    fun getSelectedStudents(): List<SelectStudents> = selectedStudents.toList()

    // Select all students
    fun selectAll() {
        selectedStudents.clear()
        selectedStudents.addAll(studentsList)
        notifyDataSetChanged()
    }

    // Clear all selections
    fun clearSelection() {
        selectedStudents.clear()
        notifyDataSetChanged()
    }

    // Check if all students are selected
    fun isAllSelected(): Boolean = studentsList.isNotEmpty() && selectedStudents.size == studentsList.size
}
