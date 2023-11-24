package com.example.kaledarz.activities

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.kaledarz.helpers.ApplicationContext
import com.example.kaledarz.DTO.Note
import com.example.kaledarz.DTO.Status
import com.example.kaledarz.R
import com.example.kaledarz.databinding.FragmentElementBinding
import com.example.kaledarz.helpers.AlarmHelper
import com.example.kaledarz.helpers.DateFormatHelper
import com.example.kaledarz.helpers.MyDatabaseHelper
import com.example.kaledarz.helpers.NotificationHelper
import com.example.kaledarz.helpers.PickerHelper
import com.example.kaledarz.helpers.PopUpManager

class ElementFragment : Fragment() {
    private var _binding: FragmentElementBinding? = null
    private val binding get() = _binding!!

    private val args: ElementFragmentArgs by navArgs()

    private lateinit var notificationHelper: NotificationHelper
    private var alarmHelper: AlarmHelper? = null
    private var myPref: SharedPreferences? = null
    private lateinit var myDB: MyDatabaseHelper
    private var note = Note()
    private lateinit var pickerHelper: PickerHelper
    private var isRedButtonSet = false

    private var activityType = "ADD"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentElementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationHelper = NotificationHelper(requireContext())
        ApplicationContext.context?.let {
            myPref = it.getSharedPreferences("run_alarms", AppCompatActivity.MODE_PRIVATE)
            alarmHelper = AlarmHelper(it)
        }
        notificationHelper.createNotificationChannel()
        myDB = MyDatabaseHelper(requireContext())
        pickerHelper = PickerHelper(requireContext())

        binding.nextDaysInfo.text = ""
        getAndSetIntentData()
        binding.startTimeButton.setOnClickListener {
            pickerHelper.runTimePicker(binding.startTimeButton)
        }

        binding.endTimeButton.setOnClickListener {
            pickerHelper.runTimePicker(binding.endTimeButton)
        }

        binding.startDateButton.setOnClickListener {
            pickerHelper.runDatePicker(binding.startDateButton)
        }

        binding.endDateButton.setOnClickListener {
            pickerHelper.runDatePicker(binding.endDateButton)
        }

        binding.deleteButton.setOnClickListener {
            binding.doneButton.isEnabled = true
            if (binding.deleteButton.text == CANCEL_INFO) { //CANCEL
                enableButtonIfCancel()
            } else {  //DELETE
                showDeleteConfirmDialog(requireContext())
            }
        }

        binding.editButton.setOnClickListener {
            if (binding.editButton.text != SAVE_INFO) { //EDIT
                enableButtonIfEdit()
            } else { //SAVE
                editNoteAndExit()
            }
        }

        binding.doneButton.setOnClickListener {
            note.done = !note.done
            refreshDoneButton()
            myDB.updateDone(note.id.toString(), note.done)
            if (note.done) {
                alarmHelper?.unsetAlarm(note.id!!)
            } else {
                alarmHelper?.setAlarm(note)
            }
            Navigation.findNavController(binding.root).popBackStack()
        }

        binding.addButton.setOnClickListener {
            validDatesAndAddNote()
        }

        binding.duplicationNumberButton.setOnClickListener {
            val popUpManager = PopUpManager(requireContext())
            popUpManager.getNumber(
                binding.duplicationNumberButton.text.toString().toInt(),
                layoutInflater.inflate(R.layout.number_picker, null),
                binding.duplicationNumberButton,
                binding.startDateButton.text.toString(),
                binding.nextDaysInfo
            )
        }

        binding.duplicationButton.setOnClickListener {
            val action = ElementFragmentDirections.actionElementFragmentSelf(
                id = null,
                type = "ADD",
                content = binding.contentText.text.toString(),
                date = null,
                startDate = binding.startDateButton.text.toString(),
                endDate = binding.endDateButton.text.toString(),
                startTime = binding.startTimeButton.text.toString(),
                endTime = binding.endTimeButton.text.toString()
            )
            Navigation.findNavController(requireView()).navigate(action)
        }
    }

    private fun addDuplicatedNotes() {
        for (i in 1..binding.duplicationNumberButton.text.toString().toInt()) {
            note.start_date = DateFormatHelper.getNextDayFromString(note.start_date)
            note.end_date = DateFormatHelper.getNextDayFromString(note.end_date)
            addNoteToDatabase()
        }
    }

    private fun createNote(): Note {

        var content = binding.contentText.text.toString().trim()
        if (content == "") {
            content = "Reminder"
        }
        return Note(
            null,
            binding.startDateButton.text.toString().trim(),
            binding.endDateButton.text.toString().trim(),
            binding.startTimeButton.text.toString().trim(),
            binding.endTimeButton.text.toString().trim(),
            content,
            false,
            "",
            Status.UNDONE
        )
    }

    private fun addNoteToDatabase() {
        val myDB = MyDatabaseHelper(requireContext())
        myDB.addGame(note)
        note.id = myDB.readLastRow().id
    }

    private fun setRedButtonIfDatesWrong() {
        isRedButtonSet = if (checkRightDate()) {
            binding.endDateText.setBackgroundColor(0x00000000)
            if (checkRightHour()) {
                binding.endTimeText.setBackgroundColor(0x00000000)
                false
            } else {
                binding.endTimeText.setBackgroundColor(Color.parseColor("#910000"))
                true
            }
        } else {
            binding.endDateText.setBackgroundColor(Color.parseColor("#910000"))
            binding.endTimeText.setBackgroundColor(0x00000000)
            true
        }
    }

    private fun checkRightDate(): Boolean {
        return DateFormatHelper.isEndDateGreaterAndEqualThanStartDate(
            binding.startDateButton.text.toString(),
            binding.endDateButton.text.toString()
        )
    }

    private fun checkRightHour(): Boolean {
        return DateFormatHelper.isEndDateEqualToStartDate(
            binding.startDateButton.text.toString(),
            binding.startDateButton.text.toString()
        ) && DateFormatHelper.isEndTimeGreaterThanStartTime(
            binding.startTimeButton.text.toString(),
            binding.endTimeButton.text.toString()
        ) || DateFormatHelper.isEndDateGreaterThanStartDate(
            binding.startDateButton.text.toString(),
            binding.endDateButton.text.toString()
        )
    }

    private fun showErrorDateDialog(c: Context) {
        var messageError = ""
        if (!checkRightDate()) {
            messageError = "Start date is later than end date"
        }
        if (!checkRightHour()
        ) {
            messageError = "End time is not later than start time"
        }
        val dialog: AlertDialog = AlertDialog.Builder(c)
            .setTitle("Date error")
            .setMessage(messageError)
            .setNegativeButton("OK", null)
            .create()
        dialog.show()
    }

    private fun showDeleteConfirmDialog(c: Context) {
        val dialog: AlertDialog = AlertDialog.Builder(c)
            .setTitle("Are you sure?")
            .setMessage("Are you sure to delete that event?")
            .setPositiveButton("Delete") { _, _ ->
                deleteNoteAndAlarm()
                Navigation.findNavController(binding.root).popBackStack()
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun getAndSetIntentData() {
        args.type?.let {
            activityType = it
            when (activityType) {
                "EDIT" -> {
                    getIntentForEditView()
                    showEditViewButton()
                    enableButtonIfEdit(false)
                    storeDataInArrays()
                    refreshDoneButton()
                }

                "ADD" -> {
                    getIntentForAddView()
                    hideEditViewButton()
                    enableButtonIfEdit()
                    setHoursOnButtons()
                }
            }
        }
    }

    private fun getIntentForEditView() {
        args.id?.let {
            note.id = it
        }
    }

    private fun getIntentForAddView() {
        binding.doneButton.text = "Add"
        args.date?.let {
            binding.startDateButton.text = it
            binding.endDateButton.text = it
        }
        args.startDate?.let {
            binding.startDateButton.text = it
        }
        args.endDate?.let {
            binding.endDateButton.text = it
        }
        args.startTime?.let {
            binding.startTimeButton.text = it
        }
        args.endTime?.let {
            binding.endTimeButton.text = it
        }
        args.content?.let {
            binding.contentText.setText(it)
        }
    }

    private fun storeDataInArrays() {
        note = myDB.readOneData(note.id.toString())
        Log.e("done:", note.done.toString())
        if (note.id == "") {
            Toast.makeText(requireContext(), "No data", Toast.LENGTH_SHORT).show()
        } else {
            binding.startDateButton.text = note.start_date
            binding.endDateButton.text = note.end_date
            binding.startTimeButton.text = note.start_time
            binding.endTimeButton.text = note.end_time
            binding.contentText.setText(note.content)
        }
    }

    private fun editNoteAndExit() {
        deleteNoteAndAlarm()
        validDatesAndAddNote()
    }

    private fun validDatesAndAddNote() {
        setRedButtonIfDatesWrong()
        if (!isRedButtonSet) {
            note = createNote()
            addNoteToDatabase()
            addDuplicatedNotes()
            Navigation.findNavController(binding.root).popBackStack()
        } else {
            showErrorDateDialog(requireContext())
        }
    }

    private fun setHoursOnButtons() {
        val hour = Calendar.getInstance()[Calendar.HOUR_OF_DAY] + 1
        binding.startTimeButton.text = DateFormatHelper.makeFullHour(hour, 0)
        binding.endTimeButton.text = DateFormatHelper.makeFullHour(23, 59)
    }

    private fun refreshDoneButton() {
        if (note.done) {
            binding.doneButton.text = "Mark as undone"
        } else {
            binding.doneButton.text = "Mark as done"
        }
    }

    private fun deleteNoteAndAlarm() {
        val myDB = MyDatabaseHelper(requireContext())
        alarmHelper?.unsetAlarm(note.id!!)
        myDB.deleteEvent(note.id!!)
    }

    private fun enableButtonIfCancel() {

        enableEditText(binding.contentText, false)
        binding.startDateButton.text = note.start_date
        binding.endDateButton.text = note.end_date
        binding.contentText.setText(note.content)
        binding.startTimeButton.text = note.start_time
        binding.endDateButton.setBackgroundResource(android.R.drawable.btn_default)
        binding.endTimeButton.setBackgroundResource(android.R.drawable.btn_default)
        enableButtonIfEdit(false)
        binding.editButton.text = EDIT_INFO
        binding.deleteButton.text = DELETE_INFO
    }

    private fun hideEditViewButton() {
        binding.editButton.visibility = View.GONE
        binding.deleteButton.visibility = View.GONE
        binding.doneButton.visibility = View.GONE
        binding.duplicationButton.visibility = View.GONE
        binding.addButton.visibility = View.VISIBLE
        binding.duplicationNumberButton.visibility = View.VISIBLE
        binding.duplicateText.visibility = View.VISIBLE
        binding.nextDaysInfo.visibility = View.VISIBLE
    }

    private fun showEditViewButton() {
        binding.editButton.visibility = View.VISIBLE
        binding.deleteButton.visibility = View.VISIBLE
        binding.doneButton.visibility = View.VISIBLE
        binding.duplicationButton.visibility = View.VISIBLE
        binding.addButton.visibility = View.GONE
        binding.duplicationNumberButton.visibility = View.GONE
        binding.duplicateText.visibility = View.GONE
        binding.nextDaysInfo.visibility = View.GONE

    }

    private fun enableEditText(editText: EditText, bool: Boolean) {
        editText.isFocusableInTouchMode = bool
        editText.isEnabled = bool
        editText.isCursorVisible = bool
    }

    private fun enableButtonIfSave() {
        enableEditText(binding.contentText, false)
        binding.editButton.text = EDIT_INFO
        binding.deleteButton.text = DELETE_INFO
        binding.doneButton.isEnabled = true
    }

    private fun enableButtonIfEdit() {
        enableEditText(binding.contentText, true)
        binding.editButton.text = SAVE_INFO
        binding.deleteButton.text = CANCEL_INFO
        enableButtonIfEdit(true)
    }

    private fun enableButtonIfEdit(bool: Boolean) {
        binding.doneButton.isEnabled = !bool
        binding.duplicationButton.isEnabled = !bool
        binding.startDateButton.isEnabled = bool
        binding.endDateButton.isEnabled = bool
        binding.startTimeButton.isEnabled = bool
        binding.endTimeButton.isEnabled = bool
        binding.contentText.isEnabled = bool
    }

    companion object {
        private const val EDIT_INFO = "EDIT"
        private const val CANCEL_INFO = "CANCEL"
        private const val DELETE_INFO = "DELETE"
        private const val SAVE_INFO = "SAVE"
    }
}
