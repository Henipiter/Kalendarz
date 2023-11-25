package com.example.kaledarz.activities

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kaledarz.DTO.Constants
import com.example.kaledarz.DTO.Note
import com.example.kaledarz.R
import com.example.kaledarz.databinding.FragmentCalendarBinding
import com.example.kaledarz.helpers.AlarmHelper
import com.example.kaledarz.helpers.DateFormatHelper
import com.example.kaledarz.helpers.MyDatabaseHelper


class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!


    private lateinit var customAdapter: CustomAdapter
    private lateinit var databaseHelper: MyDatabaseHelper
    private var alarmHelper: AlarmHelper? = null
    private var myPref: SharedPreferences? = null

    private var chooseDate = "2020-01-01"
    private var noteList: ArrayList<Note> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myPref = requireContext().getSharedPreferences("run_alarms", AppCompatActivity.MODE_PRIVATE)
        alarmHelper = AlarmHelper(requireContext())

        chooseDate = DateFormatHelper.getTodayDate(binding.calendarView.date)
        databaseHelper = MyDatabaseHelper(requireContext())

        customAdapter = CustomAdapter(requireContext(), noteList) { id ->
            val action = CalendarFragmentDirections.actionCalendarFragmentToElementFragment(
                id = id,
                type = "EDIT",
                content = null,
                date = null,
                startDate = null,
                endDate = null,
                startTime = null,
                endTime = null
            )
            Navigation.findNavController(requireView()).navigate(action)
        }
        binding.recyclerViewEvent.adapter = customAdapter
        binding.recyclerViewEvent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        storeDataInArrays()

        if (myPref?.getString(Constants.ALARM_ON_OFF, "true") == "true") {
            alarmHelper?.setAlarmForNotes(noteList)
        }
        customAdapter.notifyDataSetChanged()

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            chooseDate = DateFormatHelper.getChosenDate(year, month, dayOfMonth)
            Log.e("aa", chooseDate)
            storeDataInArrays()
            customAdapter.notifyDataSetChanged()
        }

        binding.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.add -> {
                    val action = CalendarFragmentDirections.actionCalendarFragmentToElementFragment(
                        id = null,
                        type = "ADD",
                        content = null,
                        date = chooseDate,
                        startDate = null,
                        endDate = null,
                        startTime = null,
                        endTime = null
                    )
                    Navigation.findNavController(requireView()).navigate(action)
                    true
                }

                else -> false
            }
        }


        if (!allPermissionsGranted()) {
            requestAppPermissions()
        }


    }

    override fun onResume() {
        super.onResume()
        storeDataInArrays()
        if (myPref?.getString(Constants.ALARM_ON_OFF, "true") == "true") {
            alarmHelper?.setAlarmForNotes(noteList)
        }
        customAdapter.notifyDataSetChanged()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }


    private fun storeDataInArrays() {
        filterNoteList(databaseHelper.readAllData(), chooseDate)
        if (noteList.size == 0) {
            binding.noRowsInfo.visibility = View.VISIBLE
            binding.imageMute.isVisible =
                myPref?.getString(Constants.ALARM_ON_OFF, "false") != "true"
        } else {
            binding.imageMute.visibility = View.INVISIBLE
            binding.noRowsInfo.visibility = View.INVISIBLE
            Note.computeStatusForNoteList(noteList)
        }
    }

    private fun filterNoteList(list: ArrayList<Note>, chosenDate: String) {
        noteList.clear()
        for (note in list) {
            val isAboveStart =
                DateFormatHelper.isFirstDateGreaterAndEqualToSecond(
                    chosenDate, note.start_date, "dd-MM-yyyy"
                )
            val isUnderEnd =
                DateFormatHelper.isFirstDateGreaterAndEqualToSecond(
                    note.end_date, chosenDate, "dd-MM-yyyy"
                )
            if (isAboveStart && isUnderEnd)
                noteList.add(note)
        }
    }

    private fun requestAppPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)

    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    requireContext(),
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    companion object {
        private val REQUIRED_PERMISSIONS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mutableListOf(android.Manifest.permission.POST_NOTIFICATIONS).toTypedArray()
            } else {
                arrayOf()
            }
    }
}