package com.example.kaledarz.activities

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kaledarz.DTO.Constants
import com.example.kaledarz.DTO.DateFilter
import com.example.kaledarz.DTO.Note
import com.example.kaledarz.DTO.Status
import com.example.kaledarz.R
import com.example.kaledarz.databinding.FragmentListBinding
import com.example.kaledarz.helpers.AlarmHelper
import com.example.kaledarz.helpers.DateFormatHelper
import com.example.kaledarz.helpers.MyDatabaseHelper


class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private var myPref: SharedPreferences? = null

    private lateinit var customAdapter: CustomAdapter
    private lateinit var databaseHelper: MyDatabaseHelper
    private var dateFilter = DateFilter()
    private var alarmHelper: AlarmHelper? = null

    private var originalList = ArrayList<Note>()
    private var showList = ArrayList<Note>()

    private var filterLowerStart = Constants.NONE
    private var filterLowerEnd = Constants.NONE
    private var filterUpperStart = Constants.NONE
    private var filterUpperEnd = Constants.NONE
    private var filterContent = Constants.NONE

    private var choose = Status.UNDONE


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        databaseHelper = MyDatabaseHelper(requireContext())
        alarmHelper = AlarmHelper(requireContext())


        binding.toolbar.inflateMenu(R.menu.top_menu_list)
        myPref = requireContext().getSharedPreferences("run_alarms", AppCompatActivity.MODE_PRIVATE)
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.filter -> {
                    FilterDialog(dateFilter) {
                        if (it != DateFilter()) {
                            binding.toolbar.menu.findItem(R.id.clear_filter).isVisible = true
                        }
                        dateFilter = it
                        applyFilterAndGetData()
                    }.show(childFragmentManager, "TAG")
                    true
                }

                R.id.clear_filter -> {
                    dateFilter = DateFilter()
                    applyFilterAndGetData()
                    it.isVisible = false
                    true
                }

                else -> false
            }
        }

        customAdapter = CustomAdapter(requireContext(), showList) { id ->
            val action = ListFragmentDirections.actionListFragmentToElementFragment(
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
        binding.recyclerViewSegregated.adapter = customAdapter
        binding.recyclerViewSegregated.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        applyFilterAndGetData()
        choseButton(choose)

        binding.doneImageButton.setOnLongClickListener {
            choseButton(Status.DONE)
            deleteAllRows(Status.DONE)
            true
        }
        binding.undoneImageButton.setOnLongClickListener {
            choseButton(Status.UNDONE)
            deleteAllRows(Status.UNDONE)
            true
        }
        binding.pastImageButton.setOnLongClickListener {
            choseButton(Status.PAST)
            deleteAllRows(Status.PAST)
            true
        }
        binding.futureImageButton.setOnLongClickListener {
            choseButton(Status.FUTURE)
            deleteAllRows(Status.FUTURE)
            true
        }
        binding.allImageButton.setOnLongClickListener {
            choseButton(Status.ALL)
            deleteAllRows(Status.ALL)
            true
        }

        binding.doneImageButton.setOnClickListener {
            choseButton(Status.DONE)
        }
        binding.undoneImageButton.setOnClickListener {
            choseButton(Status.UNDONE)
        }
        binding.pastImageButton.setOnClickListener {
            choseButton(Status.PAST)
        }
        binding.futureImageButton.setOnClickListener {
            choseButton(Status.FUTURE)
        }
        binding.allImageButton.setOnClickListener {
            choseButton(Status.ALL)
        }

    }

    private fun applyFilterAndGetData() {
        filterLowerStart = getValueFromIntentIfSet(dateFilter.lowerStartDate)
        filterLowerEnd = getValueFromIntentIfSet(dateFilter.lowerEndDate)
        filterUpperStart = getValueFromIntentIfSet(dateFilter.upperStartDate)
        filterUpperEnd = getValueFromIntentIfSet(dateFilter.upperEndDate)
        filterContent = getValueFromIntentIfSet(dateFilter.content)

        binding.lowerStartDateText.text = filterLowerStart
        binding.lowerEndDateText.text = filterLowerEnd
        binding.upperStartDateText.text = filterUpperStart
        binding.upperEndDateText.text = filterUpperEnd
        binding.contentText.text = filterContent

        choseButton(choose)
    }

    private fun deleteAllRows(status: Status) {

        val dialog: AlertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Delete notes")
            .setMessage("Are you sure to delete notes with status $status?")
            .setNegativeButton("CANCEL", null)
            .setNeutralButton("CLEAR") { _, _ ->
                deleteAllFromList()
                choseButton(choose)
            }
            .create()
        dialog.show()
    }


    private fun deleteAllFromList() {
        val myDatabaseHelper = MyDatabaseHelper(requireContext())
        for (note in showList) {
            alarmHelper?.unsetAlarm(note.id!!)
            myDatabaseHelper.deleteEvent(note.id!!)
        }
    }

    private fun choseButton(choose: Status) {
        getButtonStatus(this.choose).setBackgroundResource(R.color.buttonColor)
        this.choose = choose
        getButtonStatus(choose).setBackgroundResource(R.color.selectedButtonColor)
        prepareArrays(choose)
        chooseArray()
    }


    private fun getValueFromIntentIfSet(key: String): String {
        return if (key != "") {
            key
        } else {
            Constants.NONE
        }
    }

    private fun applyFilter() {
        val tempList = originalList.toList()
        for (note in tempList) {
            val isLowerStartDateNoteIsValid = filterLowerStart == Constants.NONE ||
                    DateFormatHelper.isFirstDateGreaterAndEqualToSecond(
                        note.start_date, filterLowerStart, "dd-MM-yyyy"
                    )
            val isUpperStartDateNoteIsValid = filterUpperStart == Constants.NONE ||
                    DateFormatHelper.isFirstDateGreaterAndEqualToSecond(
                        filterUpperStart, note.start_date, "dd-MM-yyyy"
                    )
            val isLowerEndDateNoteIsValid = filterLowerEnd == Constants.NONE ||
                    DateFormatHelper.isFirstDateGreaterAndEqualToSecond(
                        note.end_date, filterLowerEnd, "dd-MM-yyyy"
                    )
            val isUpperEndDateNoteIsValid = filterUpperEnd == Constants.NONE ||
                    DateFormatHelper.isFirstDateGreaterAndEqualToSecond(
                        filterUpperEnd, note.end_date, "dd-MM-yyyy"
                    )
            val isContentValid =
                filterContent == Constants.NONE || note.content != null && note.content!!.contains(
                    filterContent
                )
            if (!(isLowerStartDateNoteIsValid && isUpperStartDateNoteIsValid
                        && isLowerEndDateNoteIsValid && isUpperEndDateNoteIsValid && isContentValid)
            ) {
                originalList.remove(note)
            }
        }
    }

    private fun chooseArray() {
        if (showList.size == 0) {
            binding.noRowsInfo.isVisible = true
            binding.imageMute.isVisible =
                myPref?.getString(Constants.ALARM_ON_OFF, "true") != "true"
        } else {
            binding.noRowsInfo.isVisible = false
            binding.imageMute.isVisible = false
        }
        customAdapter.notifyDataSetChanged()

    }

    private fun getButtonStatus(choose: Status): ImageButton {
        return when (choose) {
            Status.DONE -> binding.doneImageButton
            Status.UNDONE -> binding.undoneImageButton
            Status.PAST -> binding.pastImageButton
            Status.FUTURE -> binding.futureImageButton
            Status.ALL -> binding.allImageButton
        }
    }

    private fun prepareArrays(status: Status) {
        originalList.clear()
        showList.clear()
        originalList.addAll(databaseHelper.readAllData())
        if (originalList.size != 0) {
            applyFilter()
            segregateNotes(status)
        }
    }

    private fun segregateNotes(status: Status) {
        Note.computeStatusForNoteList(originalList)
        for (note in originalList) {
            if (note.status == status || status == Status.ALL) {
                showList.add(note)
            }
        }
    }
}