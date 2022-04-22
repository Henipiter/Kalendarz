package com.example.kaledarz

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SegregatedList : AppCompatActivity() {


    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var customAdapter: CustomAdapter
    private lateinit var databaseHelper: MyDatabaseHelper

    private var noteListAll: ArrayList<Note> = ArrayList()
    private var noteListAdapter: ArrayList<Note> = ArrayList()
    private var noteListDone: ArrayList<Note> = ArrayList()
    private var noteListUndone: ArrayList<Note> = ArrayList()
    private var noteListFuture: ArrayList<Note> = ArrayList()
    private var noteListPast: ArrayList<Note> = ArrayList()

    private lateinit var buttonDone: ImageButton
    private lateinit var buttonUndone: ImageButton
    private lateinit var buttonPast: ImageButton
    private lateinit var buttonFuture: ImageButton
    private lateinit var buttonAll: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_segregated_list)

        recyclerViewEvent = findViewById(R.id.recyclerViewSegregated)
        buttonDone = findViewById(R.id.done_image_button)
        buttonUndone = findViewById(R.id.undone_image_button)
        buttonPast = findViewById(R.id.past_image_button)
        buttonFuture = findViewById(R.id.future_image_button)
        buttonAll = findViewById(R.id.all_image_button)

        databaseHelper = MyDatabaseHelper(this)
        customAdapter = CustomAdapter(this, this, noteListAdapter)
        recyclerViewEvent.adapter = customAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        prepareArrays()
        chooseArray(Status.DONE)
        buttonDone.setBackgroundResource(R.color.selectedButtonColor)


        buttonDone.setOnClickListener {
            uncheckButtons()
            buttonDone.setBackgroundResource(R.color.selectedButtonColor)
            chooseArray(Status.DONE)
        }
        buttonUndone.setOnClickListener {
            uncheckButtons()
            buttonUndone.setBackgroundResource(R.color.selectedButtonColor)
            chooseArray(Status.UNDONE)
        }
        buttonPast.setOnClickListener {
            uncheckButtons()
            buttonPast.setBackgroundResource(R.color.selectedButtonColor)
            chooseArray(Status.PAST)
        }
        buttonFuture.setOnClickListener {
            uncheckButtons()
            buttonFuture.setBackgroundResource(R.color.selectedButtonColor)
            chooseArray(Status.FUTURE)
        }
        buttonAll.setOnClickListener {
            uncheckButtons()
            buttonAll.setBackgroundResource(R.color.selectedButtonColor)
            chooseArray(Status.ALL)
        }
    }

    private fun uncheckButtons() {
        buttonDone.setBackgroundResource(R.color.buttonColor)
        buttonUndone.setBackgroundResource(R.color.buttonColor)
        buttonPast.setBackgroundResource(R.color.buttonColor)
        buttonFuture.setBackgroundResource(R.color.buttonColor)
        buttonAll.setBackgroundResource(R.color.buttonColor)
    }

    private fun prepareArrays() {
        noteListAll.clear()
        noteListAll.addAll(databaseHelper.readAllData())
        if (noteListAll.size == 0) {
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show()
        }
        segregateNotes()
    }

    private fun chooseArray(status: Status) {
        noteListAdapter.clear()
         when (status) {
            Status.DONE -> {
                noteListAdapter.addAll(noteListDone)
            }
            Status.UNDONE -> {
                noteListAdapter.addAll(noteListUndone)
            }
            Status.PAST -> {
                noteListAdapter.addAll(noteListPast)
            }
            Status.FUTURE -> {
                noteListAdapter.addAll(noteListFuture)
            }
            Status.ALL -> {
                noteListAdapter.addAll(noteListAll)
            }
        }
        customAdapter.notifyDataSetChanged()

    }

    private fun segregateNotes() {
        Note.computeStatusForNoteList(noteListAll)
        for (note in noteListAll) {
            when (note.status) {
                Status.DONE -> {
                    noteListDone.add(note)
                }
                Status.UNDONE -> {
                    noteListUndone.add(note)
                }
                Status.PAST -> {
                    noteListPast.add(note)
                }
                Status.FUTURE -> {
                    noteListFuture.add(note)
                }
            }
        }
    }
}