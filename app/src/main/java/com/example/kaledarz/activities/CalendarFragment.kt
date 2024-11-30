package com.example.kaledarz.activities

import android.content.SharedPreferences
import android.content.pm.PackageManager
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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.applandeo.materialcalendarview.CalendarDay
import com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener
import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener
import com.example.kaledarz.DTO.Constants
import com.example.kaledarz.R
import com.example.kaledarz.databinding.FragmentCalendarBinding
import com.example.kaledarz.helpers.AskPermissionHelper
import com.example.kaledarz.helpers.DateFormatHelper
import com.example.kaledarz.viewmodel.CalendarViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar


class CalendarFragment : Fragment() {

    private val calendarViewModel: CalendarViewModel by activityViewModels()
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var customAdapter: CustomAdapter
    private var myPref: SharedPreferences? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        calendarViewModel.readingNoteProcessFinished.set(false)
        Log.d("DATEE", "calendarViewModel.noteList AAAAA")
        calendarViewModel.noteList.value = ArrayList()
        binding.toolbar.inflateMenu(R.menu.top_menu_calendar)
        myPref = requireContext().getSharedPreferences("run_alarms", AppCompatActivity.MODE_PRIVATE)
        initObserver()
        Log.d("DATEE", "readAllNotes onViewCreated")
        calendarViewModel.readAllNotes()
        val calendar = Calendar.getInstance()
        binding.calendarView.setDate(calendar)


        customAdapter = CustomAdapter(requireContext(), calendarViewModel.getFilteredList()) { id ->
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

        binding.calendarView.setOnPreviousPageChangeListener(object : OnCalendarPageChangeListener {
            override fun onChange() {
                Log.w("BLOCKING", "setOnPreviousPageChangeListener freezeCalendar")
                freezeCalendar()
                val currentMonth = binding.calendarView.currentPageDate.get(Calendar.MONTH)
                val currentYear = binding.calendarView.currentPageDate.get(Calendar.YEAR)
                calendarViewModel.preparePreviousAndNextMonths(currentMonth, currentYear)
                Log.d("DATEE", "=================")
            }
        })
        binding.calendarView.setOnForwardPageChangeListener(object : OnCalendarPageChangeListener {
            override fun onChange() {
                Log.w("BLOCKING", "setOnForwardPageChangeListener freezeCalendar")
                freezeCalendar()
                val currentMonth = binding.calendarView.currentPageDate.get(Calendar.MONTH)
                val currentYear = binding.calendarView.currentPageDate.get(Calendar.YEAR)
                calendarViewModel.preparePreviousAndNextMonths(currentMonth, currentYear)
                Log.d("DATEE", "=================")
            }
        })

        binding.calendarView.setOnCalendarDayClickListener(object : OnCalendarDayClickListener {
            override fun onClick(calendarDay: CalendarDay) {
                storeDataInArrays(calendarDay.calendar)
            }
        })

        binding.progressBar.setOnLongClickListener {
            Toast.makeText(requireContext(), "Restarting", Toast.LENGTH_SHORT).show()
            binding.progressBarInfo.visibility = View.GONE
            calendarViewModel.resetProcessing()
            val currentMonth = binding.calendarView.currentPageDate.get(Calendar.MONTH)
            val currentYear = binding.calendarView.currentPageDate.get(Calendar.YEAR)
            calendarViewModel.prepareCurrent(currentMonth, currentYear)
            return@setOnLongClickListener true
        }

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.add -> {
                    val action = CalendarFragmentDirections.actionCalendarFragmentToElementFragment(
                        id = null,
                        type = "ADD",
                        content = null,
                        date = DateFormatHelper.getTodayDate(binding.calendarView.selectedDates.first().timeInMillis),
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
        Log.d("DATEE", "readAllNotes onResume")
        calendarViewModel.readAllNotes()
    }

    private fun allPermissionsGranted() = AskPermissionHelper.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }


    private fun storeDataInArrays(calendar: Calendar) {
        val chooseDate = DateFormatHelper.getTodayDate(calendar.timeInMillis)
        calendarViewModel.filterNoteList(chooseDate)
        freezeCalendar()
    }

    private fun freezeCalendar() {
        Log.w("BLOCKING", "freezeCalendar")
        binding.calendarView.setSwipeEnabled(false)
        binding.blockerView.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            delay(3000)
            if (binding.blockerView.visibility == View.VISIBLE) {
                binding.progressBarInfo.visibility = View.VISIBLE
            }
        }
    }

    private fun startCalendar() {
        Log.w("BLOCKING", "startCalendar")
        binding.calendarView.setSwipeEnabled(true)
        binding.blockerView.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    private fun initObserver() {
        calendarViewModel.currentPrepareProcessing.observe(viewLifecycleOwner) {
            Log.d("DATEE", "currentPrepareProcessing ${calendarViewModel.isPrepareProcessing()}")
            if (!it && !calendarViewModel.isPrepareProcessing()) {
                Log.d("BLOCKING", "currentPrepareProcessing start")
                startCalendar()
            }
        }
        calendarViewModel.prevAndNextPrepareProcessing.observe(viewLifecycleOwner) {
            Log.d(
                "DATEE",
                "prevAndNextPrepareProcessing ${calendarViewModel.isPrepareProcessing()}"
            )
            if (!it && !calendarViewModel.isPrepareProcessing()) {
                Log.d("BLOCKING", "prevAndNextPrepareProcessing start")
                startCalendar()
            }
        }
        calendarViewModel.updateCalendarProcessing.observe(viewLifecycleOwner) {
            Log.d("DATEE", "updateCalendarProcessing ${calendarViewModel.isPrepareProcessing()}")
            if (!it && !calendarViewModel.isPrepareProcessing()) {
                Log.d("BLOCKING", "updateCalendarProcessing start")
                startCalendar()
            }
        }
        calendarViewModel.noteList.observe(viewLifecycleOwner) {
            if (it.isEmpty() || !calendarViewModel.readingNoteProcessFinished.get()) {
                return@observe
            }
            Log.d(
                "DATEE",
                "calendarViewModel.noteList size ${calendarViewModel.noteList.value!!.size}"
            )
            val currentMonth = binding.calendarView.currentPageDate.get(Calendar.MONTH)
            val currentYear = binding.calendarView.currentPageDate.get(Calendar.YEAR)
            calendarViewModel.prepareCurrent(currentMonth, currentYear)
            storeDataInArrays(binding.calendarView.selectedDates.first())
        }
        calendarViewModel.currentPageUpdated.observe(viewLifecycleOwner) {
            if (it) {
                Log.w("BLOCKING", "currentPageUpdated freezeCalendar")
                freezeCalendar()
                val currentMonth = binding.calendarView.currentPageDate.get(Calendar.MONTH)
                val currentYear = binding.calendarView.currentPageDate.get(Calendar.YEAR)
                calendarViewModel.preparePreviousAndNextMonths(currentMonth, currentYear)
            }
        }

        calendarViewModel.calendarDaySleep.observe(viewLifecycleOwner) {
            if (it) {
                calendarViewModel.calendarDaySleep.postValue(false)
                binding.calendarView.notifyDataSetChanged()
            }
        }
        calendarViewModel.shouldUpdateGrid.observe(viewLifecycleOwner) { shouldUpdateGrid ->
            if (!shouldUpdateGrid) {
                return@observe
            }
            Log.d("EEE", "calendarDayList.observe")
            calendarViewModel.calendarDayList.value?.let {
                binding.calendarView.setCalendarDays(it)
                calendarViewModel.runCalendarDaySleep()
                Log.d("DATEE", "calendarView update")
                calendarViewModel.shouldUpdateGrid.postValue(false)
                calendarViewModel.updateCalendarProcessing.postValue(false)
            }
        }

        calendarViewModel.filteredList.observe(viewLifecycleOwner) {
            Log.d("EEE", "filteredList.observe")
            it?.let { noteList ->
                if (noteList.size == 0) {
                    binding.noRowsInfo.visibility = View.VISIBLE
                    binding.imageMute.isVisible =
                        myPref?.getString(Constants.ALARM_ON_OFF, "true") != "true"
                } else {
                    binding.imageMute.visibility = View.INVISIBLE
                    binding.noRowsInfo.visibility = View.INVISIBLE
                }
                customAdapter.noteList = noteList
                customAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun requestAppPermissions() {
        activityResultLauncher.launch(AskPermissionHelper.REQUIRED_PERMISSIONS)

        if (!AskPermissionHelper.hasExactAlarmPermission(requireContext())) {
            AskPermissionHelper.requestExactAlarmPermission(requireContext())
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in AskPermissionHelper.REQUIRED_PERMISSIONS && !it.value)
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
}