package com.example.vk

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


class CustomerAdapter : RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder>() {
    private val customers = mutableListOf<Customer>()
    private var serviceConfirmListener: OnServiceConfirmListener? = null
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance() // Firestore instance

    // Initialize Firebase Storage
    private val storage = FirebaseStorage.getInstance()

    // Create a reference to the Firestore storage location where you want to store the photos
    private val storageRef = storage.reference.child("photos")

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100 // You can use any integer value you prefer
    }

    interface OnServiceConfirmListener {
        fun onServiceConfirm(
            customer: Customer,
            problem: String,
            otherProblems: String,
            modelId: String,
            CallStatus: String
        )
    }

    fun setCustomers(customerList: List<Customer>) {
        customers.clear()
        customers.addAll(customerList)
        notifyDataSetChanged()
    }

    fun clearCustomers() {
        customers.clear()
        notifyDataSetChanged()
    }

    fun setOnServiceConfirmListener(listener: OnServiceConfirmListener) {
        serviceConfirmListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.customer_card_item, parent, false)
        return CustomerViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val customer = customers[position]
        holder.bind(customer)
    }

    override fun getItemCount(): Int {
        return customers.size
    }

    inner class CustomerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userModel: TextView = itemView.findViewById(R.id.userModelTextView)
        private val userModelId: TextView = itemView.findViewById(R.id.userModelIdTextView)
        private val amd: TextView = itemView.findViewById(R.id.amd)
        private val bookService: Button = itemView.findViewById(R.id.Book)
        private val layoutShow: LinearLayout = itemView.findViewById(R.id.layout_show)
        private val showproblem: LinearLayout = itemView.findViewById(R.id.showproblem)
        private val confirmServiceButton: Button = itemView.findViewById(R.id.cnfm_service)
        private val problemSpinner: Spinner = itemView.findViewById(R.id.problem)
        private val othersEditText: EditText = itemView.findViewById(R.id.others)
        private val uparrow: TextView = itemView.findViewById(R.id.uparrow)
        private val printerImage: ImageView = itemView.findViewById(R.id.printerImageView) // ImageView for the printer image

        init {
            confirmServiceButton.setOnClickListener {
                val problem = problemSpinner.selectedItem.toString()
                val others = othersEditText.text.toString()
                val modelId = userModelId.text.toString().replace("Machine Model Id: ", "") // Remove prefix
                val customer = customers[adapterPosition]
                val CallStatus = "Booked"
                serviceConfirmListener?.onServiceConfirm(customer, problem, others, modelId,CallStatus)
            }

            uparrow.setOnClickListener {
                val isVisible = layoutShow.visibility
                layoutShow.visibility = if (isVisible == View.VISIBLE) View.GONE else View.VISIBLE
            }


        }

        fun bind(customer: Customer) {
            userModel.text = "Machine Model: ${customer.model}"
            userModelId.text = "Machine Model Id: ${customer.modelId}"
            amd.text = "AMD: ${customer.amd}"

            // Load image using Glide
            val model = customer.model // Get the model name
            val imageResourceRef = db.collection("imageResource")

            imageResourceRef.whereEqualTo("model", model)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val imageUrl = querySnapshot.documents[0].getString("image")
                        // Load image into printerImage ImageView using Glide
                        Glide.with(itemView.context)
                            .load(imageUrl)
                            .placeholder(R.drawable.scanner_printer) // Placeholder image resource
                            .error(R.drawable.baseline_print_24) // Error image resource in case of failure
                            .into(printerImage)
                    } else {
                        // Handle if image resource not found
                        printerImage.setImageResource(R.drawable.scanner_printer) // Or handle this case in your app
                    }
                }
                .addOnFailureListener { e ->
                    // Handle error
                    Log.e("ImageLoad", "Error loading image", e)
                    printerImage.setImageResource(R.drawable.baseline_print_24) // Or handle this case in your app
                }

            val adapter = ArrayAdapter.createFromResource(
                itemView.context,
                R.array.problem,
                android.R.layout.simple_spinner_item
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            showproblem.visibility = View.GONE
            problemSpinner.adapter = adapter
            problemSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedProblem = parent?.getItemAtPosition(position).toString()
                    showproblem.visibility = if (selectedProblem == "Others") View.VISIBLE else View.GONE
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            bookService.setOnClickListener {
                val amdLowerCase = customer.amd?.lowercase()

                if (amdLowerCase == "active") {
                    val isVisible = layoutShow.visibility

                    if (isVisible == View.VISIBLE) {
                        layoutShow.visibility = View.GONE
                    } else {
                        layoutShow.visibility = View.VISIBLE
                    }
                } else {
                    showOutOfWarrantyAlertDialog(itemView.context)
                }
            }
        }

        private val REQUEST_IMAGE_CAPTURE = 1
        private val REQUEST_IMAGE_PICK = 2

        private fun showOutOfWarrantyAlertDialog(context: Context) {
            val alertDialogBuilder = AlertDialog.Builder(context)
            alertDialogBuilder.apply {
                setTitle("OUT OF AMD")
                setMessage("This product is not under AMD. The service charge ₹400 will be added. \nPlease click 'OK' to proceed.")
                setPositiveButton("OK") { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                    val isVisible = layoutShow.visibility

                    if (isVisible == View.VISIBLE) {
                        layoutShow.visibility = View.GONE
                    } else {
                        layoutShow.visibility = View.VISIBLE
                    }
                }
                setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                }
            }
            alertDialogBuilder.show()
        }
    }
}