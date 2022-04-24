package com.example.kaledarz.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kaledarz.*
import com.example.kaledarz.DTO.Constants.Companion.CONTENT
import com.example.kaledarz.DTO.Constants.Companion.LOWER_END
import com.example.kaledarz.DTO.Constants.Companion.LOWER_START
import com.example.kaledarz.DTO.Constants.Companion.NONE
import com.example.kaledarz.DTO.Constants.Companion.UPPER_END
import com.example.kaledarz.DTO.Constants.Companion.UPPER_START
import com.example.kaledarz.DTO.Note
import com.example.kaledarz.DTO.Status
import com.example.kaledarz.helpers.DateFormatHelper
import com.example.kaledarz.helpers.MyDatabaseHelper


class SegregatedListActivity : AppCompatActivity() {

    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var customAdapter: CustomAdapter
    private lateinit var databaseHelper: MyDatabaseHelper

    private var originalList = ArrayList<Note>()
    private var allList = ArrayList<Note>()
    private var showList = ArrayList<Note>()
    private var doneList = ArrayList<Note>()
    private var undoneList = ArrayList<Note>()
    private var futureList = ArrayList<Note>()
    private var pastList = ArrayList<Note>()

    private lateinit var buttonDone: ImageButton
    private lateinit var buttonUndone: ImageButton
    private lateinit var buttonPast: ImageButton
    private lateinit var buttonFuture: ImageButton
    private lateinit var buttonAll: ImageButton

    private lateinit var buttonFilter: ImageButton
    private lateinit var buttonRefresh: ImageButton

    private lateinit var noRowsInfoText: TextView
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
            prepareArrays()
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
        buttonRefresh = findViewById(R.id.refresh_button)

        noRowsInfoText = findViewById(R.id.no_rows_info)
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

        prepareArrays()
        choseButton(choose)

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

        buttonRefresh.setOnClickListener {
            prepareArrays()
            choseButton(choose)
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

    private fun choseButton(choose: Status) {
        getButtonStatus(this.choose).setBackgroundResource(R.color.buttonColor)
        this.choose = choose
        getButtonStatus(choose).setBackgroundResource(R.color.selectedButtonColor)
        chooseArray(choose)
    }


    private fun getValueFromIntentIfSet(intent: Intent, key: String): String {
        return if (intent.hasExtra(key)) {
            intent.getStringExtra(key).toString()
        } else {
            NONE
        }
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

    private fun prepareArrays() {
        originalList.clear()
        doneList.clear()
        undoneList.clear()
        pastList.clear()
        futureList.clear()
        allList.clear()
        originalList.addAll(databaseHelper.readAllData())
        if (originalList.size != 0) {
            applyFilter()
            segregateNotes()
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
            if (isLowerStartDateNoteIsValid && isUpperStartDateNoteIsValid
                && isLowerEndDateNoteIsValid && isUpperEndDateNoteIsValid && isContentValid
            ) {
                allList.add(note)
            }
        }
    }

    private fun chooseArray(status: Status) {
        showList.clear()
        when (status) {
            Status.DONE -> {
                showList.addAll(doneList)
            }
            Status.UNDONE -> {
                showList.addAll(undoneList)
            }
            Status.PAST -> {
                showList.addAll(pastList)
            }
            Status.FUTURE -> {
                showList.addAll(futureList)
            }
            Status.ALL -> {
                showList.addAll(allList)
            }
        }
        if (showList.size == 0) {
            noRowsInfoText.visibility = View.VISIBLE
        } else {
            noRowsInfoText.visibility = View.GONE
        }
        customAdapter.notifyDataSetChanged()

    }

    private fun segregateNotes() {
        Note.computeStatusForNoteList(allList)
        for (note in allList) {
            when (note.status) {
                Status.DONE -> {
                    doneList.add(note)
                }
                Status.UNDONE -> {
                    undoneList.add(note)
                }
                Status.PAST -> {
                    pastList.add(note)
                }
                Status.FUTURE -> {
                    futureList.add(note)
                }
                Status.ALL -> {
                }
            }
        }
    }
}