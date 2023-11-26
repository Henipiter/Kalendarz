package com.example.kaledarz.activities


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.kaledarz.R
import com.example.kaledarz.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        val navController = Navigation.findNavController(this, R.id.ee)
        binding!!.bottomNavigationView.selectedItemId = R.id.calendarFragment

        NavigationUI.setupWithNavController(binding!!.bottomNavigationView, navController)
    }

}

