package com.example.kaledarz.activities


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.kaledarz.R
import com.example.kaledarz.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.getRoot());
        val navController = Navigation.findNavController(this, R.id.ee)
        binding!!.bottomNavigationView.setSelectedItemId(R.id.calendarFragment);

        NavigationUI.setupWithNavController(binding!!.bottomNavigationView, navController)
    }

}

