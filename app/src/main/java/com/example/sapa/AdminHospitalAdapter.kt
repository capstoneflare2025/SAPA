import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.sapa.AdminHospitalInformationActivity
import com.example.sapa.databinding.ItemAdminHospitalBinding
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

data class AdminHospital(
    val id: Int,
    val hospitalName: String,
    val hospitalEmail: String,
    val hospitalContact: String,
    val hospitalCity: String,
    val hospitalType: String,
    val hospitalStreet: String,
    val hospitalProvince: String,
    val allocationName: String,
    val allocationSection: String,
    val allocationTimeSlot: String,
    val billingInfo: String  // New property for billing_info
)

class AdminHospitalAdapter(
    private val context: Context,
    private val adminHospitalEmail: String,  // Passing hospitalEmail
    private val adminHospitals: MutableList<AdminHospital> // List of Hospital
) : RecyclerView.Adapter<AdminHospitalAdapter.HospitalViewHolder>() {

    // Create a new ViewHolder for the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalViewHolder {
        val binding = ItemAdminHospitalBinding.inflate(LayoutInflater.from(context), parent, false)
        return HospitalViewHolder(binding)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: HospitalViewHolder, position: Int) {
        val hospital = adminHospitals[position]

        // Bind hospital data to the views
        holder.binding.txtNumber.text = hospital.id.toString()  // Display index (1-based)
        holder.binding.hospitalName.text = hospital.hospitalName  // Hospital name

        // Handle the click event of the details icon/button
        holder.binding.btnEdit.setOnClickListener {
            val intent = Intent(context, AdminHospitalInformationActivity::class.java).apply {
                putExtra("hospitalName", hospital.hospitalName)
                putExtra("hospitalEmail", hospital.hospitalEmail)
                putExtra("contact", hospital.hospitalContact)
                putExtra("city", hospital.hospitalCity)
                putExtra("type", hospital.hospitalType)
                putExtra("street", hospital.hospitalStreet)
                putExtra("province", hospital.hospitalProvince)
                putExtra("allocationName", hospital.allocationName)  // Allocation data
                putExtra("allocationSection", hospital.allocationSection)
                putExtra("timeSlot", hospital.allocationTimeSlot)
                putExtra("billingInfo", hospital.billingInfo)  // Pass billing info to the next activity
            }
            context.startActivity(intent)
        }

        // Handle the click event of the delete button
        holder.binding.btnDelete.setOnClickListener {
            // Show confirmation dialog
            showConfirmationDialog(hospital.id, hospital.hospitalEmail, position)
        }
    }

    // Return the total number of items
    override fun getItemCount(): Int = adminHospitals.size

    // Method to add a new hospital to the list and update the RecyclerView
    fun addHospital(hospital: AdminHospital) {
        adminHospitals.add(hospital)  // Add the new hospital
        notifyItemInserted(adminHospitals.size - 1)  // Notify RecyclerView to update
    }

    // Method to update the entire list and notify RecyclerView
    fun updateData(newHospitals: List<AdminHospital>) {
        adminHospitals.clear()  // Clear the current list
        adminHospitals.addAll(newHospitals)  // Add all new hospitals
        notifyDataSetChanged()  // Notify RecyclerView to refresh
    }

    // Delete hospital from both server and list
    private fun deleteHospital(hospitalId: Int, hospitalEmail: String, position: Int) {
        thread {
            try {
                // Set up the URL for the delete request
                val url = URL("http://192.168.254.193/sapa_api/add_hospital/delete_hospital.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true

                // Prepare the request parameters (hospital ID and email)
                val params = "hospital_id=$hospitalId&hospital_email=$hospitalEmail"

                // Write the parameters to the output stream
                conn.outputStream.write(params.toByteArray())

                // Check the response code
                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Successfully deleted the hospital from the server, so remove it from the list
                    (context as? Activity)?.runOnUiThread {  // Cast context to Activity
                        adminHospitals.removeAt(position)  // Remove the hospital from the list
                        notifyItemRemoved(position)  // Notify adapter to update the RecyclerView
                        Toast.makeText(context, "Hospital deleted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("Delete Error", "Failed to delete hospital, response code: $responseCode")
                    (context as? Activity)?.runOnUiThread {  // Cast context to Activity
                        Toast.makeText(context, "Failed to delete hospital", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                (context as? Activity)?.runOnUiThread {  // Cast context to Activity
                    Toast.makeText(context, "Error deleting hospital", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Show confirmation dialog before deleting the hospital
    private fun showConfirmationDialog(hospitalId: Int, hospitalEmail: String, position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Are you sure you want to delete this hospital?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                // Proceed to delete the hospital
                deleteHospital(hospitalId, hospitalEmail, position)
            }
            .setNegativeButton("No") { dialog, id ->
                // Do nothing, just dismiss the dialog
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    // ViewHolder class to hold the binding instance
    inner class HospitalViewHolder(val binding: ItemAdminHospitalBinding) :
        RecyclerView.ViewHolder(binding.root)
}
