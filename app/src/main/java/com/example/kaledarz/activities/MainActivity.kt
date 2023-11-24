package com.example.kaledarz.activities


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
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



        NavigationUI.setupWithNavController(binding!!.bottomNavigationView, navController)

//        val appBarConfiguration = AppBarConfiguration.Builder(
//            R.id.calendarFragment, R.id.settingsFragment, R.id.listFragment
//        ).build()
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

//        setContentView(binding!!.root)
//        replaceFragment(CalendarFragment())
//        binding!!.bottomNavigationView.background = null
//        binding!!.bottomNavigationView.setSelectedItemId(R.id.calendar);
//        binding!!.bottomNavigationView.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.calendar -> {
//                    replaceFragment(CalendarFragment())
//
//                }
//                R.id.setting -> replaceFragment(SettingsFragment())
//                R.id.list -> replaceFragment(ListFragment())
//            }
//            true
//        }

    }

    private fun replaceFragment(fragment: Fragment) {
//        val fragmentManager: FragmentManager = supportFragmentManager
//        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
//        fragmentTransaction.replace(R.id.frame_layout, fragment)
//        fragmentTransaction.commit()
    }
}

