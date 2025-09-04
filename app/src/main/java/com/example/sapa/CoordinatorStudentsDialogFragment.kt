package com.example.sapa

import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.sapa.databinding.DialogCoordinatorStudentsBinding

class CoordinatorStudentsDialogFragment(
    private val schoolName: String,
    private val studentsList: List<Students>
) : DialogFragment() {

    private var _binding: DialogCoordinatorStudentsBinding? = null
    private val binding get() = _binding!!
    private var filteredList: List<Students> = studentsList

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCoordinatorStudentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the school title
        val title = SpannableString("Students of\n$schoolName")
        binding.txtSchoolTitle.setText(title, TextView.BufferType.SPANNABLE)

        // Populate table initially
        populateTable(filteredList)

        // Search filter
        binding.editSearchStudent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                filteredList = if (query.isEmpty()) {
                    studentsList.filter { it.schoolName.trim().equals(schoolName.trim(), ignoreCase = true) }
                } else {
                    studentsList.filter {
                        "${it.studentFirstName} ${it.studentMiddleName} ${it.studentLastName}"
                            .lowercase().contains(query) &&
                                it.schoolName.trim().equals(schoolName.trim(), ignoreCase = true)
                    }
                }
                populateTable(filteredList)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Close button
        binding.iconBack.setOnClickListener { dismiss() }
    }


    private fun populateTable(list: List<Students>) {
        binding.tableStudents.removeAllViews()
        list.forEach { student ->
            val row = TableRow(requireContext())
            val txtFullName = TextView(requireContext()).apply {
                text = "${student.studentFirstName} ${student.studentMiddleName} ${student.studentLastName}"
                setPadding(16, 16, 16, 16)
            }
            row.addView(txtFullName)
            binding.tableStudents.addView(row)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.95).toInt(),
            (resources.displayMetrics.heightPixels * 0.85).toInt()
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


