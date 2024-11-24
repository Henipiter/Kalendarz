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
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.applandeo.materialcalendarview.CalendarDay
import com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener
import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener
import com.example.kaledarz.DTO.Constants
import com.example.kaledarz.DTO.Note
import com.example.kaledarz.DTO.Status
import com.example.kaledarz.R
import com.example.kaledarz.databinding.FragmentCalendarBinding
import com.example.kaledarz.helpers.DateFormatHelper
import com.example.kaledarz.helpers.MyDatabaseHelper
import java.util.Calendar


class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var customAdapter: CustomAdapter
    private lateinit var databaseHelper: MyDatabaseHelper
    private var myPref: SharedPreferences? = null

    private var chooseDate = "2024-01-01"
    private var currentYear = 2024
    private var currentMonth = 0
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

        binding.toolbar.inflateMenu(R.menu.top_menu_calendar)

        myPref = requireContext().getSharedPreferences("run_alarms", AppCompatActivity.MODE_PRIVATE)

        chooseDate =
            DateFormatHelper.getTodayDate(binding.calendarView.currentPageDate.timeInMillis)

        currentYear = chooseDate.substring(6).toInt()
        currentMonth = chooseDate.substring(3, 5).toInt() - 1

        databaseHelper = MyDatabaseHelper(requireContext())

        val calendar = Calendar.getInstance()
        binding.calendarView.setDate(calendar)





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

        customAdapter.notifyDataSetChanged()

        prepareCalendarEvents()

        binding.calendarView.setOnPreviousPageChangeListener(object : OnCalendarPageChangeListener {
            override fun onChange() {
                if (currentMonth == 0) {
                    currentMonth = 11
                    currentYear -= 1
                } else {
                    currentMonth -= 1
                }
                Toast.makeText(
                    requireContext(),
                    "PREVIOUS ${currentYear} ${currentMonth}",
                    Toast.LENGTH_SHORT
                ).show()
                prepareCalendarEvents()
            }
        })
        binding.calendarView.setOnForwardPageChangeListener(object : OnCalendarPageChangeListener {
            override fun onChange() {
                if (currentMonth == 11) {
                    currentMonth = 0
                    currentYear += 1
                } else {
                    currentMonth += 1
                }
                Toast.makeText(
                    requireContext(),
                    "FORWARD ${currentYear} ${currentMonth}",
                    Toast.LENGTH_SHORT
                ).show()
                prepareCalendarEvents()
            }
        })

        binding.calendarView.setOnCalendarDayClickListener(object : OnCalendarDayClickListener {
            override fun onClick(calendarDay: CalendarDay) {
                val clickedDayCalendar = calendarDay.calendar
                val year = clickedDayCalendar.get(Calendar.YEAR)
                val month = clickedDayCalendar.get(Calendar.MONTH)
                val dayOfMonth = clickedDayCalendar.get(Calendar.DAY_OF_MONTH)
                val calendar = Calendar.getInstance()
                calendar.set(year, month, dayOfMonth)
                binding.calendarView.setHighlightedDays(listOf(calendar))


                currentYear = year
                currentMonth = month
                chooseDate = DateFormatHelper.getChosenDate(year, month, dayOfMonth)
                Log.d("aa", chooseDate)
                storeDataInArrays()
                customAdapter.notifyDataSetChanged()
            }

        })

        binding.toolbar.setOnMenuItemClickListener {
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
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }


    private fun storeDataInArrays() {
        noteList.clear()
        noteList.addAll(filterNoteList(databaseHelper.readAllData(), chooseDate))
        if (noteList.size == 0) {
            binding.noRowsInfo.visibility = View.VISIBLE
            binding.imageMute.isVisible =
                myPref?.getString(Constants.ALARM_ON_OFF, "true") != "true"
        } else {
            binding.imageMute.visibility = View.INVISIBLE
            binding.noRowsInfo.visibility = View.INVISIBLE
            Note.computeStatusForNoteList(noteList)
        }
    }

    private fun filterNoteList(list: ArrayList<Note>, chosenDate: String): ArrayList<Note> {
        val noteList = ArrayList<Note>()
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
        return noteList
    }

    private fun requestAppPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)

    }

    @DrawableRes
    private fun getDrawableByStatus(statuses: Set<Status>): Int? {
        if (statuses.size == 4) {
            return R.drawable.event_four_done_undone_late_future
        }
        if (statuses.size == 3) {
            if (!statuses.contains(Status.DONE)) {
                return R.drawable.event_three_undone_past_future
            }
            if (!statuses.contains(Status.UNDONE)) {
                return R.drawable.event_three_done_past_future
            }
            if (!statuses.contains(Status.PAST)) {
                return R.drawable.event_three_done_undone_future
            }
            if (!statuses.contains(Status.FUTURE)) {
                return R.drawable.event_three_done_undone_past
            }
        }
        if (statuses.size == 2) {
            if (statuses.contains(Status.DONE) && statuses.contains(Status.UNDONE)) {
                return R.drawable.event_two_done_undone
            }
            if (statuses.contains(Status.DONE) && statuses.contains(Status.PAST)) {
                return R.drawable.event_two_done_past
            }
            if (statuses.contains(Status.DONE) && statuses.contains(Status.FUTURE)) {
                return R.drawable.event_two_done_future
            }
            if (statuses.contains(Status.UNDONE) && statuses.contains(Status.PAST)) {
                return R.drawable.event_two_undone_past
            }
            if (statuses.contains(Status.UNDONE) && statuses.contains(Status.FUTURE)) {
                return R.drawable.event_two_undone_future
            }
            if (statuses.contains(Status.PAST) && statuses.contains(Status.FUTURE)) {
                return R.drawable.event_two_past_future
            }
        }
        if (statuses.size == 1) {
            if (statuses.contains(Status.DONE)) {
                return R.drawable.image_square_done
            }
            if (statuses.contains(Status.UNDONE)) {
                return R.drawable.image_square_undone
            }
            if (statuses.contains(Status.PAST)) {
                return R.drawable.image_square_late
            }
            if (statuses.contains(Status.FUTURE)) {
                return R.drawable.image_square_future
            }
        }
        return null
    }

    private fun prepareCalendarEvents() {
        val lastDay = DateFormatHelper.getLastDayOfMonth(currentYear, currentMonth + 1)

        val calendarDayList = arrayListOf<CalendarDay>()
        val list = databaseHelper.readAllData()
        for (i in 1..lastDay) {
            val currentDate = String.format("%02d", i) + "-" + String.format(
                "%02d",
                currentMonth + 1
            ) + "-" + currentYear
            val notes = filterNoteList(list, currentDate)
            if (notes.isNotEmpty()) {
                Note.computeStatusForNoteList(notes)
                val set = HashSet<Status>()
                notes.forEach { note -> set.add(note.status) }

                calendarDayList.add(
                    getCalendarDay(
                        currentYear,
                        currentMonth,
                        i,
                        getDrawableByStatus((set))
                    )
                )
            }
        }
        binding.calendarView.setCalendarDays(calendarDayList)
    }

    private fun getCalendarDay(
        year: Int, month: Int, day: Int, @DrawableRes imageResource: Int?
    ): CalendarDay {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        val calendarDay = CalendarDay(calendar)
        calendarDay.imageResource = imageResource
        return calendarDay
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