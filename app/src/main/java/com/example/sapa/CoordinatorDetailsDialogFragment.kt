package com.example.sapa

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.sapa.databinding.DialogCoordinatorDetailsBinding

class CoordinatorDetailsDialogFragment : DialogFragment() {
    private var _binding: DialogCoordinatorDetailsBinding? = null
    private val binding get() = _binding!!

    private var coordinatorFullName: String? = null
    private var coordinatorContact: String? = null
    private var coordiantorEmail: String? = null
    private var coordinatorUserName: String? = null
    private var coordinatorBirthday: String? = null
    private var coordinatorGender: String? = null
    private var coordiantorStatus: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            coordinatorFullName = it.getString(ARG_COORDINATOR_FULLNAME)
            coordinatorContact = it.getString(ARG_COORDINATOR_CONTACT)
            coordiantorEmail = it.getString(ARG_COORDINATOR_EMAIL)
            coordinatorUserName = it.getString(ARG_COORDINATOR_USERNAME)
            coordinatorBirthday = it.getString(ARG_COORDINATOR_BIRTHDAY)
            coordinatorGender = it.getString(ARG_COORDINATOR_GENDER)
            coordiantorStatus = it.getString(ARG_COORDINATOR_STATUS)
        }
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCoordinatorDetailsBinding.inflate(inflater, container, false)

        // Bind all data
        binding.coordinatorFullName.text = coordinatorFullName
        binding.coordinatorContact.text = coordinatorContact
        binding.coordinatorEmail.text = coordiantorEmail
        binding.coordinatorUserName.text = coordinatorUserName
        binding.coordinatorBirthday.text = coordinatorBirthday
        binding.coordinatorGender.text = coordinatorGender
        binding.coordinatorStatus.text = coordiantorStatus

//        binding.btnAccept.setOnClickListener { dismiss() }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_COORDINATOR_FULLNAME = "coordinator_full_name"
        private const val ARG_COORDINATOR_CONTACT = "coordinator_contact"
        private const val ARG_COORDINATOR_EMAIL = "coordinator_email"
        private const val ARG_COORDINATOR_USERNAME = "coordinator_username"
        private const val ARG_COORDINATOR_BIRTHDAY = "coordinator_birthday"
        private const val ARG_COORDINATOR_GENDER = "coordinator_gender"
        private const val ARG_COORDINATOR_STATUS = "coordinator_status"

        fun newInstance(
            coordinatorFullName: String,
            coordinatorContact: String,
            coordinatorEmail: String,
            coordinatorUserName: String,
            coordinatorBirthday: String,
            coordinatorGender: String,
            coordinatorStatus: String
        ) = CoordinatorDetailsDialogFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_COORDINATOR_FULLNAME, coordinatorFullName)
                putString(ARG_COORDINATOR_CONTACT, coordinatorContact)
                putString(ARG_COORDINATOR_EMAIL, coordinatorEmail)
                putString(ARG_COORDINATOR_USERNAME, coordinatorUserName)
                putString(ARG_COORDINATOR_BIRTHDAY, coordinatorBirthday)
                putString(ARG_COORDINATOR_GENDER, coordinatorGender)
                putString(ARG_COORDINATOR_STATUS, coordinatorStatus)
            }
        }
    }
}
