package com.example.vk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import com.etebarian.meowbottomnavigation.MeowBottomNavigation

class bottomNavigation (private val activity: AppCompatActivity, private val selectedItemId: Int) {
    private val bottomNavigation: MeowBottomNavigation

    init {
        bottomNavigation = activity.findViewById(R.id.bottomNavigation)

        bottomNavigation.add(MeowBottomNavigation.Model(1, R.drawable.baseline_home_24))
        bottomNavigation.add(MeowBottomNavigation.Model(2, R.drawable.baseline_notifications_24))
        bottomNavigation.add(MeowBottomNavigation.Model(3, R.drawable.baseline_person_24))

        bottomNavigation.show(selectedItemId, enableAnimation = false)

        bottomNavigation.setOnClickMenuListener { model: MeowBottomNavigation.Model? ->
            model?.let {
                when (it.id) {
                    1 -> {
                        if (selectedItemId != 1) {
                            activity.startActivity(Intent(activity, Home::class.java))
                            activity.finish()
                        }
                    }
                    2 -> {
                        if (selectedItemId != 2) {
                            activity.startActivity(Intent(activity, Notification::class.java))
                            activity.finish()
                        }
                    }
                    3 -> {
                        if (selectedItemId != 3) {
                            activity.startActivity(Intent(activity, Profile::class.java))
                            activity.finish()
                        }
                    }
                }
            }
        }
    }

    // Function to handle back press when using custom navigation bar
    fun onBackPressed(): Boolean {
        if (selectedItemId != 1) {
            bottomNavigation.show(1, enableAnimation = false)
            activity.startActivity(Intent(activity, Home::class.java))
            activity.finish()
            return true
        }
        return false
    }
}
