package com.example.kaledarz.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kaledarz.DTO.Constants
import com.example.kaledarz.DTO.Constants.Companion.CONTENT
import com.example.kaledarz.DTO.Constants.Companion.LOWER_END
import com.example.kaledarz.DTO.Constants.Companion.LOWER_START
import com.example.kaledarz.DTO.Constants.Companion.NONE
import com.example.kaledarz.DTO.Constants.Companion.UPPER_END
import com.example.kaledarz.DTO.Constants.Companion.UPPER_START
import com.example.kaledarz.DTO.Note
import com.example.kaledarz.DTO.Status
import com.example.kaledarz.R
import com.example.kaledarz.helpers.DateFormatHelper
import com.example.kaledarz.helpers.MyDatabaseHelper


class SegregatedListActivity : AppCompatActivity() {

    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var customAdapter: CustomAdapter
    private lateinit var databaseHelper: MyDatabaseHelper

    private var originalList = ArrayList<Note>()
    private var showList = ArrayList<Note>()

    private lateinit var buttonDone: ImageButton
    private lateinit var buttonUndone: ImageButton
    private lateinit var buttonPast: ImageButton
    private lateinit var buttonFuture: ImageButton
    private lateinit var buttonAll: ImageButton

    private lateinit var buttonFilter: ImageButton

    private lateinit var noRowsInfoText: TextView
    private lateinit var imageMute: ImageView
    private lateinit var lowerStartDateText: TextView
    private lateinit var lowerEndDateText: TextView
    private lateinit var upperStartDateText: TextView
    private lateinit var upperEndDateText: TextView

    private lateinit var textContent: TextView

    private var filterLowerStart = NONE
    private var filterLowerEnd = NONE
    private var filterUpperStart = NONE
    private var filterUpperEnd = NONE
    private var filterContent = NONE

    private var choose = Status.UNDONE

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    filterLowerStart = getValueFromIntentIfSet(data, LOWER_START)
                    filterLowerEnd = getValueFromIntentIfSet(data, LOWER_END)
                    filterUpperStart = getValueFromIntentIfSet(data, UPPER_START)
                    filterUpperEnd = getValueFromIntentIfSet(data, UPPER_END)
                    filterContent = getValueFromIntentIfSet(data, CONTENT)

                    lowerStartDateText.text = filterLowerStart
                    lowerEndDateText.text = filterLowerEnd
                    upperStartDateText.text = filterUpperStart
                    upperEndDateText.text = filterUpperEnd
                    textContent.text = filterContent
                }
            }
            choseButton(choose)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_segregated_list)

        recyclerViewEvent = findViewById(R.id.recyclerViewSegregated)
        buttonDone = findViewById(R.id.done_image_button)
        buttonUndone = findViewById(R.id.undone_image_button)
        buttonPast = findViewById(R.id.past_image_button)
        buttonFuture = findViewById(R.id.future_image_button)
        buttonAll = findViewById(R.id.all_image_button)

        buttonFilter = findViewById(R.id.filter_button)

        noRowsInfoText = findViewById(R.id.no_rows_info)
        imageMute = findViewById(R.id.imageMute2)
        lowerStartDateText = findViewById(R.id.text_lower_start_date)
        lowerEndDateText = findViewById(R.id.text_end_lower_date)
        upperStartDateText = findViewById(R.id.text_upper_start_date)
        upperEndDateText = findViewById(R.id.text_upper_end_date)

        textContent = findViewById(R.id.text_content)

        lowerStartDateText.text = filterLowerStart
        lowerEndDateText.text = filterLowerEnd
        upperStartDateText.text = filterUpperStart
        upperEndDateText.text = filterUpperEnd
        textContent.text = filterUpperEnd

        databaseHelper = MyDatabaseHelper(this)
        customAdapter = CustomAdapter(this, this, showList)
        recyclerViewEvent.adapter = customAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        choseButton(choose)

        buttonDone.setOnLongClickListener {
            choseButton(Status.DONE)
            deleteAllRows(Status.DONE)
            true
        }
        buttonUndone.setOnLongClickListener {
            choseButton(Status.UNDONE)
            deleteAllRows(Status.UNDONE)
            true
        }
        buttonPast.setOnLongClickListener {
            choseButton(Status.PAST)
            deleteAllRows(Status.PAST)
            true
        }
        buttonFuture.setOnLongClickListener {
            choseButton(Status.FUTURE)
            deleteAllRows(Status.FUTURE)
            true
        }
        buttonAll.setOnLongClickListener {
            choseButton(Status.ALL)
            deleteAllRows(Status.ALL)
            true
        }

        buttonDone.setOnClickListener {
            choseButton(Status.DONE)
        }
        buttonUndone.setOnClickListener {
            choseButton(Status.UNDONE)
        }
        buttonPast.setOnClickListener {
            choseButton(Status.PAST)
        }
        buttonFuture.setOnClickListener {
            choseButton(Status.FUTURE)
        }
        buttonAll.setOnClickListener {
            choseButton(Status.ALL)
        }

        buttonFilter.setOnClickListener {
            val intent = Intent(this, FilterListActivity::class.java)
            intent.putExtra(LOWER_START, filterLowerStart)
            intent.putExtra(LOWER_END, filterLowerEnd)
            intent.putExtra(UPPER_START, filterUpperStart)
            intent.putExtra(UPPER_END, filterUpperEnd)
            intent.putExtra(CONTENT, filterContent)
            resultLauncher.launch(intent)
        }
    }

    private fun deleteAllRows(status: Status) {

        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setTitle("Delete notes")
            .setMessage("Are you sure to delete notes with status $status?")
            .setNegativeButton("CANCEL", null)
            .setNeutralButton("CLEAR") { dialog, which ->
                deleteAllFromList()
                choseButton(choose)
            }
            .create()
        dialog.show()
    }


    private fun deleteAllFromList() {
        val myDatabaseHelper = MyDatabaseHelper(this)
        for (note in showList) {
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


    private fun getValueFromIntentIfSet(intent: Intent, key: String): String {
        return if (intent.hasExtra(key)) {
            intent.getStringExtra(key).toString()
        } else {
            NONE
        }
    }

    private fun applyFilter() {
        for (note in originalList) {
            val isLowerStartDateNoteIsValid = filterLowerStart == NONE ||
                    DateFormatHelper.isFirstDateGreaterAndEqualToSecond(
                        note.start_date, filterLowerStart, "dd-MM-yyyy"
                    )
            val isUpperStartDateNoteIsValid = filterUpperStart == NONE ||
                    DateFormatHelper.isFirstDateGreaterAndEqualToSecond(
                        filterUpperStart, note.start_date, "dd-MM-yyyy"
                    )
            val isLowerEndDateNoteIsValid = filterLowerEnd == NONE ||
                    DateFormatHelper.isFirstDateGreaterAndEqualToSecond(
                        note.end_date, filterLowerEnd, "dd-MM-yyyy"
                    )
            val isUpperEndDateNoteIsValid = filterUpperEnd == NONE ||
                    DateFormatHelper.isFirstDateGreaterAndEqualToSecond(
                        filterUpperEnd, note.end_date, "dd-MM-yyyy"
                    )
            val isContentValid =
                filterContent == NONE || note.content != null && note.content!!.contains(
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
            noRowsInfoText.isVisible = true
            val myPref = applicationContext.getSharedPreferences("run_alarms", MODE_PRIVATE)
            imageMute.isVisible = myPref.getString(Constants.ALARM_ON_OFF, "false") != "true"
        } else {
            noRowsInfoText.isVisible = false
            imageMute.isVisible = false
        }
        customAdapter.notifyDataSetChanged()

    }

    private fun getButtonStatus(choose: Status): ImageButton {
        return when (choose) {
            Status.DONE -> buttonDone
            Status.UNDONE -> buttonUndone
            Status.PAST -> buttonPast
            Status.FUTURE -> buttonFuture
            Status.ALL -> buttonAll
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