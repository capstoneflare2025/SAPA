package com.example.sapa

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sapa.databinding.ItemPendingCoordinatorsBinding

// ✅ Coordinator Model (fields match your DB columns)
data class Coordinator(
    val id: Int,
    val coordinatorUsername: String,
    val coordinatorEmail: String,
    val coordinatorFirstName: String,
    val coordinatorMiddleName: String,
    val coordinatorLastName: String,
    val coordinatorContact: String,
    val coordinatorBirthday: String,
    val coordinatorGender: String,
    var coordinatorStatus: String
) {
    // ✅ Helper to display full name
    fun getFullName(): String {
        return listOf(coordinatorFirstName, coordinatorMiddleName, coordinatorLastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
    }
}

// Adapter
class UserAdapter(
    private val coordinators: MutableList<Coordinator>,
    private val onItemClick: (Coordinator) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemPendingCoordinatorsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding =
            ItemPendingCoordinatorsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val coordinator = coordinators[position]

        // Show full name in list
        holder.binding.coordinatorName.text = coordinator.getFullName()
        holder.binding.coordinatorStatus.text = coordinator.coordinatorStatus

        holder.binding.root.setOnClickListener { onItemClick(coordinator) }
    }

    fun updateList(newList: List<Coordinator>) {
        coordinators.clear()
        coordinators.addAll(newList)
        notifyDataSetChanged()
    }

    fun addCoordinatorAtTop(coordinator: Coordinator) {
        coordinators.add(0, coordinator)
        notifyItemInserted(0)
    }


    override fun getItemCount(): Int = coordinators.size
}
