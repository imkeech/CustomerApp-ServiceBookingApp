package com.example.vk

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Home : AppCompatActivity() {
    private lateinit var customBottomNavigationView: bottomNavigation
    private lateinit var recyclerView: RecyclerView
    private lateinit var customerAdapter: CustomerAdapter

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var serviceCount: Int = 0 // Initialize service count to 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        customerAdapter = CustomerAdapter()
        recyclerView.adapter = customerAdapter

        customBottomNavigationView = bottomNavigation(this, 1)

        db = FirebaseFirestore.getInstance()
        customerAdapter.setOnServiceConfirmListener(object :
            CustomerAdapter.OnServiceConfirmListener {
            override fun onServiceConfirm(
                customer: Customer,
                problem: String,
                others: String,
                modelId: String,
                CallStatus: String
            ) {
                saveServiceBooking(customer, problem, others, modelId,CallStatus)
            }
        })

        fetchServiceCount() // Fetch the current service count
        fetchAndDisplayUserData()
    }

    private fun fetchServiceCount() {
        // Query the Firestore to get the current service count
        db.collection("Service_Booking")
            .get()
            .addOnSuccessListener { querySnapshot ->
                serviceCount =
                    querySnapshot.size() // Update service count based on the number of existing service bookings
            }
            .addOnFailureListener { e ->
                Log.e("HomeActivity", "Error fetching service count", e)
                Toast.makeText(this, "Error fetching service count.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchAndDisplayUserData() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val phoneNumber = user.phoneNumber
            Log.d("HomeActivity", "Phone Number: $phoneNumber")

            val customerDetailsRef = db.collection("customerDetails")

            customerDetailsRef.whereEqualTo("ph_no", phoneNumber)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val customerList = mutableListOf<Customer>()

                        for (document in querySnapshot) {
                            val model = document.getString("model")
                            val modelId = document.getString("modelId")
                            val installationDate = document.getString("installationeDate")
                            val amdperiod = document.getString("amdperiod")
                            val no_of_coppies = document.getString("no_of_coppies")
                            val userName = document.getString("name")

                            val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                            val editor = sharedPref.edit()

                            editor.putString(
                                "user_name",
                                userName
                            ) // Replace userName with the actual user name
                            editor.putString(
                                "phone_number",
                                phoneNumber
                            ) // Replace phoneNumber with the actual phone number

                            editor.apply()

                            if (installationDate != null) {
                                if (amdperiod != null) {
                                    if (installationDate.isNotEmpty() && amdperiod.isNotEmpty()) {
                                        val sdf =
                                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        val currentDate = Calendar.getInstance().time
                                        val parsedInstallationDate = sdf.parse(installationDate)

                                        if (parsedInstallationDate != null) {
                                            val amdPeriodMonths = amdperiod.toInt()
                                            val calendar = Calendar.getInstance()
                                            calendar.time = parsedInstallationDate
                                            calendar.add(Calendar.MONTH, amdPeriodMonths)
                                            val amdEndDate = calendar.time

                                            // Calculate targetDate
                                            calendar.time = parsedInstallationDate
                                            calendar.add(
                                                Calendar.DAY_OF_MONTH,
                                                amdPeriodMonths * 30
                                            )
                                            val targetDate = calendar.time

                                            val remainingCoppies =
                                                50000 - no_of_coppies.toString().toInt()
                                            val remainingDays =
                                                (targetDate.time - currentDate.time) / (24 * 60 * 60 * 1000)

                                            // Check if currentDate is between installationDate and targetDate
                                            if (currentDate in parsedInstallationDate..targetDate && no_of_coppies.toString()
                                                    .toInt() <= 50000
                                            ) {
                                                Log.d("AMDCheck", "AMD is available")
                                                customerDetailsRef.document(document.id)
                                                    .update("AMD", "Active")

                                            } else {
                                                Log.d("AMDCheck", "AMD is not available")
                                                customerDetailsRef.document(document.id)
                                                    .update("AMD", "Expired")

                                            }

                                            // Log values for debugging
                                            Log.d("DateComparison", "Current Date: $currentDate")
                                            Log.d(
                                                "DateComparison",
                                                "Installation Date: $parsedInstallationDate"
                                            )
                                            Log.d("DateComparison", "Target Date: $targetDate")
                                            Log.d(
                                                "Remaining",
                                                "Remaining Coppies: $remainingCoppies"
                                            )
                                            Log.d("Remaining", "Remaining Days: $remainingDays")

                                        } else {
                                            Log.e(
                                                "DateParsing",
                                                "Error parsing installationDate: $installationDate"
                                            )
                                        }
                                    } else {
                                        Log.e(
                                            "DateParsing",
                                            "InstallationDate or amdperiod is empty"
                                        )
                                    }
                                }
                            }

                            val amd = document.getString("AMD")
                            val customer = Customer(model, modelId, amd, no_of_coppies)
                            customerList.add(customer)
                        }

                        customerAdapter.setCustomers(customerList)
                    } else {
                        Log.d(
                            "HomeActivity",
                            "No document found for the phone number: $phoneNumber"
                        )
                        customerAdapter.clearCustomers()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("HomeActivity", "Error fetching user data", e)
                    customerAdapter.clearCustomers()
                }
        }
    }

    private fun saveServiceBooking(
        customer: Customer,
        problem: String,
        others: String,
        modelId: String,
        CallStatus:String
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid
            val phone = user.phoneNumber

            // Get the current date
            val currentDate = SimpleDateFormat("ddMMyyyy").format(Date())

            serviceCount++ // Increment the service count

            // Combine the date, 'R', and the service count to create the service ID
            val serviceId = currentDate + "R" + serviceCount

            val serviceBooking = ServiceBooking(others, problem, modelId, phone, serviceId,CallStatus)

            // Save the service booking with serviceId as the document name
            db.collection("Service_Booking")
                .document(serviceId)
                .set(serviceBooking)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Service booking saved with ID: $serviceId",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    Log.e("HomeActivity", "Error saving service booking", e)
                    Toast.makeText(this, "Error saving service booking.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onBackPressed() {
        if (!customBottomNavigationView.onBackPressed()) {
            super.onBackPressed()
        }
    }
}