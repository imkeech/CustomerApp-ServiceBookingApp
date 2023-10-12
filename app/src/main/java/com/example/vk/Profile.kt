package com.example.vk

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.location.Address
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class Profile : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var logoutButton: ImageView
    private lateinit var infoButton: Button
    private lateinit var infoCard: View
    private var isCardExpanded = false
    private lateinit var customBottomNavigationView: bottomNavigation
    private val db = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        mAuth = FirebaseAuth.getInstance()
        logoutButton = findViewById(R.id.logout)

        logoutButton.setOnClickListener {
            mAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        val name =findViewById<TextView>(R.id.Name)
        val Editname =findViewById<EditText>(R.id.editName)
        val edit = findViewById<ImageView>(R.id.edit)
        val Editphone_no =findViewById<EditText>(R.id.editphone_no)
        val phone_no = findViewById<TextView>(R.id.phone_no)
        val Editemail =findViewById<EditText>(R.id.editemail)
        val email = findViewById<TextView>(R.id.email)
        val Editaddress =findViewById<EditText>(R.id.editaddress)
        val Address = findViewById<TextView>(R.id.Address)
        val Editcity =findViewById<EditText>(R.id.editcity)
        val City = findViewById<TextView>(R.id.city)
        val Editstate =findViewById<EditText>(R.id.editstate)
        val State = findViewById<TextView>(R.id.state)
        val Editpin =findViewById<EditText>(R.id.editpin)
        val Pincode = findViewById<TextView>(R.id.pincode)
        val savebutton = findViewById<Button>(R.id.saveButton)

        edit.setOnClickListener{
            name.visibility = View.GONE
            phone_no.visibility = View.GONE
            email.visibility = View.GONE
            Address.visibility = View.GONE
            City.visibility = View.GONE
            State.visibility = View.GONE
            Pincode.visibility = View.GONE

            Editname.visibility = View.VISIBLE
            Editphone_no.visibility = View.VISIBLE
            Editemail.visibility = View.VISIBLE
            Editaddress.visibility = View.VISIBLE
            Editcity.visibility = View.VISIBLE
            Editstate.visibility = View.VISIBLE
            Editpin.visibility = View.VISIBLE
            savebutton.visibility = View.VISIBLE

        }



        val myServicesButton = findViewById<Button>(R.id.myServicesButton)
        val myservicecard = findViewById<LinearLayout>(R.id.mybookingsView)
        myServicesButton.setOnClickListener {

            val slideUp: Animation = AnimationUtils.loadAnimation(this, R.anim.slide_up)
            val slideDown: Animation = AnimationUtils.loadAnimation(this, R.anim.slide_down)
            if (myservicecard.visibility == View.VISIBLE) {
                myservicecard.startAnimation(slideUp)
                myservicecard.visibility = View.GONE
            } else {
                myservicecard.startAnimation(slideDown)
                myservicecard.visibility = View.VISIBLE
            }

        }


        val historyButton = findViewById<Button>(R.id.trdButton)
        val historyView = findViewById<LinearLayout>(R.id.historyView)
        historyButton.setOnClickListener {

            val slideUp: Animation = AnimationUtils.loadAnimation(this, R.anim.slide_up)
            val slideDown: Animation = AnimationUtils.loadAnimation(this, R.anim.slide_down)
            if (historyView.visibility == View.VISIBLE) {
                historyView.startAnimation(slideUp)
                historyView.visibility = View.GONE
            } else {
                historyView.startAnimation(slideDown)
                historyView.visibility = View.VISIBLE
            }

        }

        infoButton = findViewById(R.id.personalInfoButton)
        infoCard = findViewById(R.id.infoCard)

        infoButton.setOnClickListener {

            val slideUp: Animation = AnimationUtils.loadAnimation(this, R.anim.slide_up)
            val slideDown: Animation = AnimationUtils.loadAnimation(this, R.anim.slide_down)
            if (infoCard.visibility == View.VISIBLE) {
                infoCard.startAnimation(slideUp)
                infoCard.visibility = View.GONE
            } else {
                infoCard.startAnimation(slideDown)
                infoCard.visibility = View.VISIBLE
            }

            isCardExpanded = !isCardExpanded
            updateInfoButtonDrawable()
        }

        // Retrieve the user's name and phone number from SharedPreferences
        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userName = sharedPref.getString("user_name", "")
        val phoneNumber = sharedPref.getString("phone_number", "")

        // Find the TextViews in the activity_profile.xml layout
        val nameTextView = findViewById<TextView>(R.id.profileName)
        val phNoTextView = findViewById<TextView>(R.id.ph_no)

        // Set the user's name and phone number in the TextViews
        nameTextView.text = userName
        phNoTextView.text = phoneNumber

        // Selected item for ProfileActivity is 3
        customBottomNavigationView = bottomNavigation(this, 3)

        if (phoneNumber != null) {
            fetchServiceBookings(phoneNumber)
        }

        fetchCustomerDetails(phoneNumber)

        if (phoneNumber != null) {
            fetchServiceBookings1(phoneNumber)
        }

    }

    private fun updateInfoButtonDrawable() {
        if (isCardExpanded) {
            infoButton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.baseline_person_24, 0, R.drawable.baseline_arrow_drop_up_24, 0)
        } else {
            infoButton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.baseline_person_24, 0, R.drawable.baseline_arrow_drop_down_24, 0)
        }
    }

    private fun fetchCustomerDetails(phoneNumber: String?) {
        if (phoneNumber != null) {
            val customerDetailsRef = db.collection("customerDetails")
            customerDetailsRef
                .whereEqualTo("ph_no", phoneNumber)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val document = documents.documents[0]
                        val customerName = document.getString("name")
                        val address = document.getString("address")
                        val city = document.getString("city")
                        val email = document.getString("email")
                        val pincode = document.getString("pincode")
                        val state = document.getString("state")

                        val nameTextView = findViewById<TextView>(R.id.Name)
                        val phone_no = findViewById<TextView>(R.id.phone_no)
                        val addressTextView = findViewById<TextView>(R.id.Address)
                        val cityTextView = findViewById<TextView>(R.id.city)
                        val emailTextView = findViewById<TextView>(R.id.email)
                        val pincodeTextView = findViewById<TextView>(R.id.pincode)
                        val stateTextView = findViewById<TextView>(R.id.state)

                        nameTextView.text = customerName
                        phone_no.text = phoneNumber
                        addressTextView.text = address
                        cityTextView.text = city
                        emailTextView.text = email
                        pincodeTextView.text = pincode
                        stateTextView.text = state
                    } else {
                        Log.d("Profile", "No matching documents")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("Profile", "Error getting documents: ", exception)
                }
        }
    }

    private fun fetchServiceBookings(phoneNumber: String) {
        val serviceBookingsRef = db.collection("Service_Booking")
        serviceBookingsRef
            .whereEqualTo("ph_no", phoneNumber)
            .whereIn("callStatus", listOf("On Hold", "Booked"))
            .get()
            .addOnSuccessListener { documents ->
                val mybookingsLayout = findViewById<LinearLayout>(R.id.mybookings)
                for (document in documents) {
                    val others = document.getString("others")
                    val problem = document.getString("problem")
                    val modelId = document.getString("modelId")
                    val serviceId = document.getString("serviceId")
                    val callStatus = document.getString("callStatus")

                    Log.d("fetchServiceBookings", "$others\n $problem\n $modelId\n $serviceId\n $callStatus")

                    val cardView = createServiceBookingCard(others, problem, modelId, serviceId, callStatus)
                    mybookingsLayout.addView(cardView)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("ServiceBooking", "Error getting service bookings: $exception")
            }
    }

    private fun createServiceBookingCard(
        others: String?,
        problem: String?,
        modelId: String?,
        serviceId: String?,
        callStatus: String?
    ): CardView {
        val cardView = CardView(this)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardView.radius = resources.getDimension(R.dimen.card_corner_radius)
        layoutParams.setMargins(0, 0, 0, resources.getDimensionPixelSize(R.dimen.card_margin_bottom))
        cardView.layoutParams = layoutParams

        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL


        // Create and add TextView for 'Call Status' field
        val statusIdTextView = TextView(this)
        val statusText = Html.fromHtml("<b>Booking Status: $callStatus</b>")
        statusIdTextView.text = statusText
        val padding = resources.getDimensionPixelSize(R.dimen.card_padding)
        statusIdTextView.setPadding(padding, padding, padding, 0)
        linearLayout.addView(statusIdTextView)


        // Create and add TextView for 'Others' field
        val othersTextView = TextView(this)
        val othersText = Html.fromHtml("<b>Others:</b> $others")
        othersTextView.text = othersText
        othersTextView.setPadding(padding, 0, padding, 0)
        linearLayout.addView(othersTextView)

        // Create and add TextView for 'Problem' field
        val problemTextView = TextView(this)
        val problemText = Html.fromHtml("<b>Problems:</b> $problem")
        problemTextView.text = problemText
        problemTextView.setPadding(padding, 0, padding, 0)
        linearLayout.addView(problemTextView)

        // Create and add TextView for 'Model ID' field
        val modelIdTextView = TextView(this)
        val modelIdText = Html.fromHtml("<b>Model ID:</b> $modelId")
        modelIdTextView.text = modelIdText
        modelIdTextView.setPadding(padding, 0, padding, 0)
        linearLayout.addView(modelIdTextView)

        // Create and add TextView for 'Service ID' field
        val serviceIdTextView = TextView(this)
        val serviceText = Html.fromHtml("<b>Service ID:</b> $serviceId")
        serviceIdTextView.text = serviceText
        serviceIdTextView.setPadding(padding, 0, padding, padding)
        linearLayout.addView(serviceIdTextView)

        cardView.addView(linearLayout)

        return cardView
    }




    private fun fetchServiceBookings1(phoneNumber: String) {
        val serviceBookingsRef = db.collection("Service_Booking")
        serviceBookingsRef
            .whereEqualTo("ph_no", phoneNumber)
            .whereIn("callStatus", listOf("Completed", "Cancled"))
            .get()
            .addOnSuccessListener { documents ->
                val mybookingsLayout1 = findViewById<LinearLayout>(R.id.history)
                for (document in documents) {
                    val others = document.getString("others")
                    val problem = document.getString("problem")
                    val modelId = document.getString("modelId")
                    val serviceId = document.getString("serviceId")
                    val callStatus = document.getString("callStatus")

                    Log.d("fetchServiceBookings", "$others\n $problem\n $modelId\n $serviceId\n $callStatus")

                    val cardView1 = createServiceBookingCard1(others, problem, modelId, serviceId, callStatus)
                    mybookingsLayout1.addView(cardView1)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("ServiceBooking", "Error getting service bookings: $exception")
            }
    }

    private fun createServiceBookingCard1(
        others: String?,
        problem: String?,
        modelId: String?,
        serviceId: String?,
        callStatus: String?
    ): CardView {
        val cardView = CardView(this)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardView.radius = resources.getDimension(R.dimen.card_corner_radius)
        layoutParams.setMargins(0, 0, 0, resources.getDimensionPixelSize(R.dimen.card_margin_bottom))
        cardView.layoutParams = layoutParams

        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL


        // Create and add TextView for 'Call Status' field
        val statusIdTextView = TextView(this)
        val statusText = Html.fromHtml("<b>Booking Status: $callStatus</b>")
        statusIdTextView.text = statusText
        val padding = resources.getDimensionPixelSize(R.dimen.card_padding)
        statusIdTextView.setPadding(padding, padding, padding, 0)
        linearLayout.addView(statusIdTextView)


        // Create and add TextView for 'Others' field
        val othersTextView = TextView(this)
        val othersText = Html.fromHtml("<b>Others:</b> $others")
        othersTextView.text = othersText
        othersTextView.setPadding(padding, 0, padding, 0)
        linearLayout.addView(othersTextView)

        // Create and add TextView for 'Problem' field
        val problemTextView = TextView(this)
        val problemText = Html.fromHtml("<b>Problems:</b> $problem")
        problemTextView.text = problemText
        problemTextView.setPadding(padding, 0, padding, 0)
        linearLayout.addView(problemTextView)

        // Create and add TextView for 'Model ID' field
        val modelIdTextView = TextView(this)
        val modelIdText = Html.fromHtml("<b>Model ID:</b> $modelId")
        modelIdTextView.text = modelIdText
        modelIdTextView.setPadding(padding, 0, padding, 0)
        linearLayout.addView(modelIdTextView)

        // Create and add TextView for 'Service ID' field
        val serviceIdTextView = TextView(this)
        val serviceText = Html.fromHtml("<b>Service ID:</b> $serviceId")
        serviceIdTextView.text = serviceText
        serviceIdTextView.setPadding(padding, 0, padding, padding)
        linearLayout.addView(serviceIdTextView)

        cardView.addView(linearLayout)

        return cardView
    }

    override fun onBackPressed() {
        if (!customBottomNavigationView.onBackPressed()) {
            super.onBackPressed()
        }
    }
}
